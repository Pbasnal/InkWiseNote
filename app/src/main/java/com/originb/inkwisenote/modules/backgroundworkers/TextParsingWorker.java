package com.originb.inkwisenote.modules.backgroundworkers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.config.AppSecrets;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.data.config.AppState;
import com.originb.inkwisenote.data.dao.NoteOcrTextDao;
import com.originb.inkwisenote.data.dao.NoteTaskStatusDao;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.entities.notedata.NoteOcrText;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskName;
import com.originb.inkwisenote.data.entities.tasks.TfIdfRelationTasks;
import com.originb.inkwisenote.io.NoteBitmapFiles;
import com.originb.inkwisenote.io.ocr.AzureOcrResult;
import com.originb.inkwisenote.io.ocr.OcrService;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStatus;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStage;
import com.originb.inkwisenote.modules.commonutils.Strings;
import com.originb.inkwisenote.modules.functionalUtils.Try;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TextParsingWorker extends Worker {
    private final NoteBitmapFiles bitmapRepository;

    private SmartNotebookRepository smartNotebookRepository;

    private final AppSecrets appSecrets;
    private final NoteTaskStatusDao noteTaskStatusDao;
    private final NoteOcrTextDao noteOcrTextDao;


    private final DebugContext debugContext = new DebugContext("TextParsingWorker");

    public TextParsingWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.bitmapRepository = Repositories.getInstance().getBitmapRepository();
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();

        noteTaskStatusDao = Repositories.getInstance().getNotesDb().noteTaskStatusDao();
        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();

        appSecrets = ConfigReader.getInstance().getAppConfig().getAppSecrets();
    }

    @NotNull
    @Override
    public Result doWork() {
        Optional<Long> noteIdOpt = Try.to(() -> getInputData().getLong("note_id", -1), debugContext).get();
        noteIdOpt.ifPresent(this::parseTextForNoteId);


        Optional<Long> bookIdOpt = Try.to(() -> getInputData().getLong("book_id", -1), debugContext).get();
        bookIdOpt.ifPresent(this::parseTextForNotebook);

        return Result.success();
    }

    public void parseTextForNotebook(long bookId) {
        Optional<SmartNotebook> smartBookOpt = smartNotebookRepository.getSmartNotebook(bookId);
        if(!smartBookOpt.isPresent()) return;
        SmartNotebook smartNotebook = smartBookOpt.get();

        smartNotebook.getAtomicNotes().stream().map(AtomicNoteEntity::getNoteId)
                .map(this::parseTextForNoteId);
    }

    public Result parseTextForNoteId(long noteId) {
        if (!isNoteIdGreaterThan0(noteId) || !validateJobStatus(noteId)) return onFailure(noteId);

        Optional<Bitmap> bitmapOpt = bitmapRepository.getFullBitmap(noteId);
        if (!bitmapOpt.isPresent()) return onFailure(noteId);
        Bitmap noteBitmap = bitmapOpt.get();

        Optional<AzureOcrResult> azureResultOpt = applyAzureOcr(noteBitmap);
        if (!azureResultOpt.isPresent()) return onFailure(noteId);
        NoteOcrText noteOcrText = new NoteOcrText(noteId, azureResultOpt.get().readResult.content);

        List<NoteOcrText> noteOcrTexts = noteOcrTextDao.readTextFromDb(noteOcrText.getNoteId());
        if (CollectionUtils.isEmpty(noteOcrTexts)) {
            noteOcrTextDao.insertTextToDb(noteOcrText);
        } else {
            noteOcrTextDao.updateTextToDb(noteOcrText);
        }

        if (!Strings.isNullOrWhitespace(noteOcrText.getExtractedText())) {
            noteTaskStatusDao.updateNoteTask(TfIdfRelationTasks.tokenizationTask(noteId));
            AppState.getInstance().setNoteStatus(noteId, NoteTaskStage.TOKENIZATION);

            WorkManagerBus.scheduleWorkForTextProcessing(getApplicationContext(), noteId);
        }

        return Result.success();

    }

    private Result onFailure(Long noteId) {
        noteTaskStatusDao.updateNoteTask(TfIdfRelationTasks.completeTask(noteId));
        AppState.getInstance().setNoteStatus(noteId, NoteTaskStage.NOTE_READY);
        WorkManagerBus.scheduleWorkForFindingRelatedNotes(getApplicationContext(), noteId);
        return ListenableWorker.Result.failure();
    }

    private boolean isNoteIdGreaterThan0(long noteId) {
        if (noteId == -1) {
            Log.e(debugContext.getDebugInfo(), "Got incorrect note id (-1) as input");
            return false;
        }
        return true;
    }

    private boolean validateJobStatus(Long noteId) {
        NoteTaskStatus jobStatus = noteTaskStatusDao.getNoteStatus(noteId, NoteTaskName.TF_IDF_RELATION);
        if (Objects.isNull(jobStatus)) {
            AppState.getInstance().setNoteStatus(noteId, NoteTaskStage.NOTE_READY);
            return false;
        }
        if (!NoteTaskStage.TEXT_PARSING.equals(jobStatus.getStage())) {
            AppState.getInstance().setNoteStatus(noteId, NoteTaskStage.NOTE_READY);
            debugContext.logError("Note is not in TEXT_PARSING stage. " + jobStatus);
            return false;
        }

        return true;
    }

    private Optional<AzureOcrResult> applyAzureOcr(Bitmap bitmap) {
        Log.d("got bitmap", "bitmap");
        Optional<AzureOcrResult> ocrResult = Try.to(() -> {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    // Compress the bitmap into the ByteArrayOutputStream
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                    // Convert the ByteArrayOutputStream to an InputStream
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    InputStream imageStream = new ByteArrayInputStream(byteArray);
                    OcrService.AnalyzeImageTask imageTask = new OcrService.AnalyzeImageTask(appSecrets);
                    AzureOcrResult azureOcrResult = imageTask.runOcr(imageStream);

                    Log.d("NoteActivity", "Ocr result: " + azureOcrResult);

                    return azureOcrResult;
                }, new DebugContext("TextParsingJob"))
                .logIfError("Failed to convert handwriting to text")
                .get();
        return ocrResult;
    }
}
