package com.originb.inkwisenote2.modules.noterelation.worker;

import android.content.Context;
import android.util.Log;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.functionalUtils.Function2;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus;
import com.originb.inkwisenote2.functionalUtils.Either;
import com.originb.inkwisenote2.common.Strings;
import com.originb.inkwisenote2.functionalUtils.Try;
import com.originb.inkwisenote2.modules.noterelation.service.NoteTfIdfLogic;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TextProcessingWorker extends Worker {
    private NoteTfIdfLogic noteTfIdfLogic;
    private NoteOcrTextDao noteOcrTextDao;
    private TextNotesDao textNotesDao;

    private final Logger logger = new Logger("TextProcessingWorker");

    private final SmartNotebookRepository smartNotebookRepository;

    public TextProcessingWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.noteTfIdfLogic = new NoteTfIdfLogic(Repositories.getInstance());
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();

        this.textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();
        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();
    }

    @NotNull
    @Override
    public Result doWork() {
        Try.to(() -> getInputData().getLong("book_id", -1), logger)
                .get()
                .filter(this::isNoteIdGreaterThan0)
                .ifPresent(this::processTextForNotebook);

        return Result.success();
    }

    private void processTextForNotebook(long bookId) {
        logger.debug("Processing text of book (new flow). noteId: " + bookId);
        Optional<SmartNotebook> smartBookOpt = smartNotebookRepository.getSmartNotebooks(bookId);
        if (!smartBookOpt.isPresent()) return;
        SmartNotebook smartNotebook = smartBookOpt.get();

        EventBus.getDefault().post(new Events.NoteStatus(smartNotebook, TextProcessingStage.TOKENIZATION));
        logger.debug("Notebook bookId: " + bookId + " contains number of notes: " + smartNotebook.getAtomicNotes().size());
        for (AtomicNoteEntity atomicNote : smartNotebook.getAtomicNotes()) {
            String text;
            if (NoteType.HANDWRITTEN_PNG.toString().equals(atomicNote.getNoteType())) {
                text = getHandwrittenNoteText(atomicNote);
            } else {
                text = getTextNoteText(atomicNote.getNoteId());
            }
            processTextForHandwrittenNote(atomicNote, text);
        }

        WorkManagerBus.scheduleWorkForFindingRelatedNotesForBook(getApplicationContext(), bookId);
    }

    private String getTextNoteText(long bookId) {
        TextNoteEntity textNoteEntity = textNotesDao.getTextNoteForNote(bookId);
        return textNoteEntity.getNoteText();
    }

    private String getHandwrittenNoteText(AtomicNoteEntity atomicNote) {
        NoteOcrText noteOcrTexts = noteOcrTextDao.readTextFromDb(atomicNote.getNoteId());
        logger.debug("Ocr text of note: " + atomicNote.getNoteId(), noteOcrTexts);

        return noteOcrTexts.getExtractedText();
    }

    private void processTextForHandwrittenNote(AtomicNoteEntity atomicNote, String text) {

        Either<Exception, DocumentTerms> eitherTerms = handleException(this::extractTermsFromNote,
                atomicNote.getNoteId(), text);
        handleException(this::createBiRelationalGraph, atomicNote.getNoteId(), eitherTerms.result);
    }

    private boolean isNoteIdGreaterThan0(long noteId) {
        if (noteId == -1) {
            logger.error("Got incorrect note id (-1) as input");
            return false;
        }
        return true;
    }

    private <A, B, R> Either<Exception, R> handleException(Function2<A, B, R> function, A input1, B input2) {
        try {
            return Either.result(function.apply(input1, input2));
        } catch (Exception ex) {
            Log.e("TextProcessingJob", "failed to process", ex);
            return Either.error(ex);
        }
    }

    private DocumentTerms extractTermsFromNote(long noteId, String text) {
        DocumentTerms documentTerms = new DocumentTerms();
        documentTerms.documentId = noteId;

        if (text == null || text.isEmpty()) {
            logger.debug("Note has empty text, skipping: " + noteId, text);
            return documentTerms; // Return an empty list for null or empty text
        }

        // Step 1: Normalize the text to lowercase
        text = text.toLowerCase();

        // Step 2: Remove non-alphanumeric characters except spaces
        text = text.replaceAll("[^a-z0-9\\s]", "");

        // Step 3: Split the text into terms by whitespace
        String[] terms = text.split("\\s+");
        documentTerms.terms = new ArrayList<>();
        for (String term : terms) {
            if (term.isEmpty()) continue;
            if (Strings.isNumber(term)) continue;

            documentTerms.terms.add(term);
        }
        logger.debug("Extracted document terms", documentTerms);
        return documentTerms;
    }

    private Long createBiRelationalGraph(long noteId, DocumentTerms documentTerms) {
        if (Objects.isNull(documentTerms)) {
            logger.debug("empty document terms, skipping: " + noteId);
            return null;
        }
        if (CollectionUtils.isEmpty(documentTerms.terms)) {
            logger.debug("terms list is empty, skipping: " + noteId);
            return documentTerms.documentId;
        }
        noteTfIdfLogic.addOrUpdateNote(documentTerms.documentId,
                documentTerms.terms);

        return documentTerms.documentId;
    }

    @Getter
    private static class DocumentTerms {
        protected Long documentId;
        protected List<String> terms;
    }
}
