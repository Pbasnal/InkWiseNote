package com.originb.inkwisenote.modules.noterelation.worker;

import android.content.Context;
import android.util.Log;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote.common.Logger;
import com.originb.inkwisenote.modules.backgroundjobs.Events;
import com.originb.inkwisenote.modules.noterelation.data.TextProcessingStage;
import com.originb.inkwisenote.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote.modules.backgroundjobs.WorkManagerBus;
import com.originb.inkwisenote.functionalUtils.Either;
import com.originb.inkwisenote.common.Strings;
import com.originb.inkwisenote.functionalUtils.Try;
import com.originb.inkwisenote.modules.noterelation.service.NoteTfIdfLogic;
import com.originb.inkwisenote.modules.ocr.data.NoteOcrText;
import com.originb.inkwisenote.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class TextProcessingWorker extends Worker {
    private NoteTfIdfLogic noteTfIdfLogic;
    private NoteOcrTextDao noteOcrTextDao;
    private final Logger logger = new Logger("TextProcessingWorker");

    private final SmartNotebookRepository smartNotebookRepository;

    public TextProcessingWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.noteTfIdfLogic = new NoteTfIdfLogic(Repositories.getInstance());
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();

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
            processTextForHandwrittenNote(atomicNote);
        }

        WorkManagerBus.scheduleWorkForFindingRelatedNotesForBook(getApplicationContext(), bookId);
    }

    private void processTextForHandwrittenNote(AtomicNoteEntity atomicNote) {
        NoteOcrText noteOcrTexts = noteOcrTextDao.readTextFromDb(atomicNote.getNoteId());
        logger.debug("Ocr text of note: " + atomicNote.getNoteId(), noteOcrTexts);

        Either<Exception, DocumentTerms> eitherTerms = handleException(this::extractTermsFromNote, noteOcrTexts);
        handleException(
                (docTerms) -> createBiRelationalGraph(atomicNote.getNoteId(), docTerms), eitherTerms.result);

    }

    private boolean isNoteIdGreaterThan0(long noteId) {
        if (noteId == -1) {
            logger.error("Got incorrect note id (-1) as input");
            return false;
        }
        return true;
    }

    private <T, R> Either<Exception, R> handleException(Function<T, R> function, T input) {
        try {
            return Either.result(function.apply(input));
        } catch (Exception ex) {
            Log.e("TextProcessingJob", "failed to process", ex);
            return Either.error(ex);
        }
    }

    private DocumentTerms extractTermsFromNote(NoteOcrText noteOcrText) {
        DocumentTerms documentTerms = new DocumentTerms();
        documentTerms.documentId = noteOcrText.getNoteId();

        String text = noteOcrText.getExtractedText();
        if (text == null || text.isEmpty()) {
            logger.debug("Note has empty text, skipping", noteOcrText);
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
