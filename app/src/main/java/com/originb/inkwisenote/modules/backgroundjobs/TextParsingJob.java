package com.originb.inkwisenote.modules.backgroundjobs;

import android.graphics.Bitmap;
import android.util.Log;
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
import com.originb.inkwisenote.modules.backgroundjobs.data.TextProcessingJobStatus;
import com.originb.inkwisenote.modules.backgroundjobs.data.TextProcessingStage;
import com.originb.inkwisenote.modules.commonutils.Strings;
import com.originb.inkwisenote.modules.functionalUtils.Try;
import com.originb.inkwisenote.modules.repositories.Repositories;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TextParsingJob extends AsyncJob {
    private final NoteBitmapFiles bitmapRepository;

    private final AppSecrets appSecrets;
    private final TextProcessingJobContract.TextProcessingDbQueries textProcessingJobDbHelper;
    private final NoteTextContract.NoteTextDbHelper noteTextDbHelper;

    TextParsingJob() {
        this.bitmapRepository = Repositories.getInstance().getBitmapRepository();

        textProcessingJobDbHelper = Repositories.getInstance().getTextProcessingJobDbHelper();
        noteTextDbHelper = Repositories.getInstance().getNoteTextDbHelper();

        appSecrets = ConfigReader.getInstance().getAppConfig().getAppSecrets();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Perform your background task here
        TextProcessingJobStatus jobStatus = textProcessingJobDbHelper.readFirstNoteAtStage(TextProcessingStage.TEXT_PARSING);
        if (Objects.isNull(jobStatus)) return null;
        if (!TextProcessingStage.TEXT_PARSING.isEqualTo(jobStatus.getStage())) {
            Log.e("TextParsingJob", "The fetched job status is not text parsing. " + jobStatus);
            return null;
        }
        Log.d("TextParsingJob", "Parsing note ");

        bitmapRepository.getFullBitmap(jobStatus.getNoteId())
                .flatMap(this::applyAzureOcr)
                .filter(res -> !Strings.isNullOrWhitespace(res.readResult.content))
                .map(res -> new NoteOcrText(jobStatus.getNoteId(), res.readResult.content))
                .ifPresent(noteOcrText -> {
                    List<NoteOcrText> noteOcrTexts = noteTextDbHelper.readTextFromDb(noteOcrText.getNoteId());
                    if (CollectionUtils.isEmpty(noteOcrTexts)) {
                        noteTextDbHelper.insertTextToDb(noteOcrText);
                    } else {
                        noteTextDbHelper.updateTextToDb(noteOcrText);
                    }

                    textProcessingJobDbHelper.updateTextToDb(jobStatus.getNoteId(), TextProcessingStage.TOKENIZATION);
                });
        return null;
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
