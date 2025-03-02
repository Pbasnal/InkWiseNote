package com.originb.inkwisenote.modules.backgroundworkers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote.Logger;
import com.originb.inkwisenote.data.config.AppState;
import com.originb.inkwisenote.data.dao.noteocr.NoteOcrTextDao;
import com.originb.inkwisenote.data.dao.tasks.NoteTaskStatusDao;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.entities.noteocrdata.NoteOcrText;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskName;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStatus;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStage;
import com.originb.inkwisenote.modules.functionalUtils.Either;
import com.originb.inkwisenote.modules.commonutils.Strings;
import com.originb.inkwisenote.modules.functionalUtils.Try;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote.modules.tfidf.NoteTfIdfLogic;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class TextProcessingWorker extends Worker {
    private NoteTfIdfLogic noteTfIdfLogic;
    private NoteTaskStatusDao noteTaskStatusDao;
    private NoteOcrTextDao noteOcrTextDao;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Logger logger = new Logger("TextProcessingWorker");

    private final SmartNotebookRepository smartNotebookRepository;

    public TextProcessingWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.noteTfIdfLogic = new NoteTfIdfLogic(Repositories.getInstance());
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();

        noteTaskStatusDao = Repositories.getInstance().getNotesDb().noteTaskStatusDao();
        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();
    }

    @NotNull
    @Override
    public Result doWork() {

        Optional<Long> noteIdOpt = Try.to(() -> getInputData().getLong("note_id", -1), logger)
                .get();
        noteIdOpt.ifPresent(noteId -> {
            if (isNoteIdGreaterThan0(noteId) && validateJobStatus(noteId)) processText(noteId);
        });


        Optional<Long> bookIdOpt = Try.to(() -> getInputData().getLong("book_id", -1), logger)
                .get();
        bookIdOpt.ifPresent(bookId -> {
            if (isNoteIdGreaterThan0(bookId)) processTextForNotebook(bookId);
        });

        return Result.success();
    }

    private void processTextForNotebook(long bookId) {
        logger.debug("Processing text of book (new flow). noteId: " + bookId);
        Optional<SmartNotebook> smartBookOpt = smartNotebookRepository.getSmartNotebook(bookId);
        if (!smartBookOpt.isPresent()) return;
        SmartNotebook smartNotebook = smartBookOpt.get();

        logger.debug("Notebook bookId: " + bookId + " contains number of notes: " + smartNotebook.getAtomicNotes().size());
        for (AtomicNoteEntity atomicNote : smartNotebook.getAtomicNotes()) {
            processTextForHandwrittenNote(atomicNote);
        }

        AppState.getInstance().setNoteStatus(bookId, NoteTaskStage.NOTE_READY);

        WorkManagerBus.scheduleWorkForFindingRelatedNotesForBook(getApplicationContext(), bookId);
    }

    private void processTextForHandwrittenNote(AtomicNoteEntity atomicNote) {
        NoteOcrText noteOcrTexts = noteOcrTextDao.readTextFromDb(atomicNote.getNoteId());
        logger.debug("Ocr text of note: " + atomicNote.getNoteId(), noteOcrTexts);

        Either<Exception, DocumentTerms> eitherTerms = handleException(this::extractTermsFromNote, noteOcrTexts);
        Either<Exception, Long> eitherDocId = handleException(
                (docTerms) -> createBiRelationalGraph(atomicNote.getNoteId(), docTerms), eitherTerms.result);
        deleteTextJob(eitherDocId);
    }

    private Result processText(long noteId) {
        logger.debug("Processing text of note (old flow). noteId: " + noteId);
        Optional<Long> noteIdOpt = Try.to(() -> getInputData().getLong("note_id", -1), logger).get();
        NoteOcrText noteOcrTexts = noteOcrTextDao.readTextFromDb(noteId);

        Either<Exception, DocumentTerms> eitherTerms = handleException(this::extractTermsFromNote, noteOcrTexts);
        Either<Exception, Long> eitherDocId = handleException(
                (docTerms) -> createBiRelationalGraph(noteId, docTerms), eitherTerms.result);
        deleteTextJob(eitherDocId);

        AppState.getInstance().setNoteStatus(noteIdOpt.get(), NoteTaskStage.NOTE_READY);

        WorkManagerBus.scheduleWorkForFindingRelatedNotes(getApplicationContext(), noteIdOpt.get());

        return Result.success();
    }

    private boolean isNoteIdGreaterThan0(long noteId) {
        if (noteId == -1) {
            logger.error("Got incorrect note id (-1) as input");
            return false;
        }
        return true;
    }

    private boolean validateJobStatus(Long noteId) {
        NoteTaskStatus jobStatus = noteTaskStatusDao.getNoteStatus(noteId, NoteTaskName.TF_IDF_RELATION);
        if (Objects.isNull(jobStatus)) return false;
        if (NoteTaskStage.TOKENIZATION != jobStatus.getStage()) {
            logger.error("Note is not in TEXT_PARSING stage. " + jobStatus);
            return false;
        }

        return true;
    }

    private void deleteTextJob(Either<Exception, Long> eitherResult) {
        noteTaskStatusDao.deleteNoteTask(eitherResult.result, NoteTaskName.TF_IDF_RELATION);
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
