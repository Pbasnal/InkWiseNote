package com.originb.inkwisenote.modules.backgroundworkers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.config.AppSecrets;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.data.notedata.NoteOcrText;
import com.originb.inkwisenote.io.NoteBitmapFiles;
import com.originb.inkwisenote.io.ocr.AzureOcrResult;
import com.originb.inkwisenote.io.ocr.OcrService;
import com.originb.inkwisenote.io.sql.NoteTextContract;
import com.originb.inkwisenote.io.sql.TextProcessingJobContract;
import com.originb.inkwisenote.data.backgroundjobs.TextProcessingJobStatus;
import com.originb.inkwisenote.data.backgroundjobs.TextProcessingStage;
import com.originb.inkwisenote.modules.commonutils.Strings;
import com.originb.inkwisenote.modules.functionalUtils.Try;
import com.originb.inkwisenote.modules.repositories.Repositories;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TextParsingWorker extends Worker {
    private final NoteBitmapFiles bitmapRepository;

    private final AppSecrets appSecrets;
    private final TextProcessingJobContract.TextProcessingDbQueries textProcessingJobDbHelper;
    private final NoteTextContract.NoteTextDbHelper noteTextDbHelper;


    private final DebugContext debugContext = new DebugContext("TextParsingWorker");

    public TextParsingWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.bitmapRepository = Repositories.getInstance().getBitmapRepository();

        textProcessingJobDbHelper = Repositories.getInstance().getTextProcessingJobDbHelper();
        noteTextDbHelper = Repositories.getInstance().getNoteTextDbHelper();

        appSecrets = ConfigReader.getInstance().getAppConfig().getAppSecrets();
    }

    @NotNull
    @Override
    public Result doWork() {
        return parseText();
    }

    public Result parseText() {
        Optional<Long> noteIdOpt = Try.to(() -> getInputData().getLong("note_id", -1), debugContext).get();

        return noteIdOpt.filter(this::isNoteIdGreaterThan0)
                .map(this::validateJobStatus)
                .flatMap(bitmapRepository::getFullBitmap)
                .flatMap(this::applyAzureOcr)
                .filter(res -> !Strings.isNullOrWhitespace(res.readResult.content))
                .map(res -> new NoteOcrText(noteIdOpt.get(), res.readResult.content))
                .map(noteOcrText -> {
                    List<NoteOcrText> noteOcrTexts = noteTextDbHelper.readTextFromDb(noteOcrText.getNoteId());
                    if (CollectionUtils.isEmpty(noteOcrTexts)) {
                        noteTextDbHelper.insertTextToDb(noteOcrText);
                    } else {
                        noteTextDbHelper.updateTextToDb(noteOcrText);
                    }

                    textProcessingJobDbHelper.updateTextToDb(noteIdOpt.get(), TextProcessingStage.TOKENIZATION);

                    WorkManagerBus.scheduleWorkForTextProcessing(getApplicationContext(), noteIdOpt.get());

                    return Result.success();
                }).orElse(Result.failure());
    }

    private boolean isNoteIdGreaterThan0(long noteId) {
        if (noteId == -1) {
            Log.e(debugContext.getDebugInfo(), "Got incorrect note id (-1) as input");
            return false;
        }
        return true;
    }

    private Long validateJobStatus(Long noteId) {
        TextProcessingJobStatus jobStatus = textProcessingJobDbHelper.getNoteStatus(noteId);
        if (Objects.isNull(jobStatus)) return null;
        if (!TextProcessingStage.TEXT_PARSING.isEqualTo(jobStatus.getStage())) {
            debugContext.logError("Note is not in TEXT_PARSING stage. " + jobStatus);
            return null;
        }

        return noteId;
    }

    private Optional<AzureOcrResult> applyAzureOcr(Bitmap bitmap) {
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
