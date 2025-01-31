package com.originb.inkwisenote.modules.backgroundworkers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.data.config.AppState;
import com.originb.inkwisenote.data.dao.NoteOcrTextDao;
import com.originb.inkwisenote.data.dao.NoteTaskStatusDao;
import com.originb.inkwisenote.data.entities.notedata.NoteOcrText;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskName;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStatus;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStage;
import com.originb.inkwisenote.modules.functionalUtils.Either;
import com.originb.inkwisenote.modules.commonutils.Strings;
import com.originb.inkwisenote.modules.functionalUtils.Try;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.tfidf.NoteTfIdfLogic;
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
    private final DebugContext debugContext = new DebugContext("TextProcessingWorker");

    public TextProcessingWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.noteTfIdfLogic = new NoteTfIdfLogic(Repositories.getInstance());

        noteTaskStatusDao = Repositories.getInstance().getNotesDb().noteTaskStatusDao();
        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();
    }

    @NotNull
    @Override
    public Result doWork() {
        return processText();
    }

    public Result processText() {
        Optional<Long> noteIdOpt = Try.to(() -> getInputData().getLong("note_id", -1), debugContext).get();
        List<NoteOcrText> noteOcrTexts = noteIdOpt.filter(this::isNoteIdGreaterThan0)
                .map(this::validateJobStatus)
                .map(noteOcrTextDao::readTextFromDb)
                .orElse(new ArrayList<>());

        noteOcrTexts.stream()
                .map(note -> handleException(this::extractTermsFromNote, note))
                .map(eitherTerms -> handleException(this::createBiRelationalGraph, eitherTerms.result))
                .forEach(this::deleteTextJob);

        AppState.getInstance().setNoteStatus(noteIdOpt.get(), NoteTaskStage.NOTE_READY);

        WorkManagerBus.scheduleWorkForFindingRelatedNotes(getApplicationContext(), noteIdOpt.get());

        return Result.success();
    }

    private boolean isNoteIdGreaterThan0(long noteId) {
        if (noteId == -1) {
            Log.e(debugContext.getDebugInfo(), "Got incorrect note id (-1) as input");
            return false;
        }
        return true;
    }

    private Long validateJobStatus(Long noteId) {
        NoteTaskStatus jobStatus = noteTaskStatusDao.getNoteStatus(noteId, NoteTaskName.TF_IDF_RELATION);
        if (Objects.isNull(jobStatus)) return null;
        if (NoteTaskStage.TOKENIZATION != jobStatus.getStage()) {
            debugContext.logError("Note is not in TEXT_PARSING stage. " + jobStatus);
            return null;
        }

        return noteId;
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

        return documentTerms;
    }

    private Long createBiRelationalGraph(DocumentTerms documentTerms) {
        if (Objects.isNull(documentTerms)) return null;
        if (CollectionUtils.isEmpty(documentTerms.terms)) return documentTerms.documentId;
        noteTfIdfLogic.addOrUpdateNote(documentTerms.documentId,
                        documentTerms.terms);

        return documentTerms.documentId;
    }

    private static class DocumentTerms {
        protected Long documentId;
        protected List<String> terms;
    }
}
