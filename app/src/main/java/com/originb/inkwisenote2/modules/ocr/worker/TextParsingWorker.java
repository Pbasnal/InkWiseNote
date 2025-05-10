package com.originb.inkwisenote2.modules.ocr.worker;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;
import com.google.mlkit.vision.digitalink.Ink;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.common.BitmapScale;
import com.originb.inkwisenote2.config.AppConfig;
import com.originb.inkwisenote2.config.AppSecrets;
import com.originb.inkwisenote2.config.ConfigReader;
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteEntity;
import com.originb.inkwisenote2.modules.handwrittennotes.data.Stroke;
import com.originb.inkwisenote2.modules.handwrittennotes.data.StrokePoint;
import com.originb.inkwisenote2.modules.ocr.data.AzureOcrResult;
import com.originb.inkwisenote2.modules.ocr.data.OcrService;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.common.ListUtils;
import com.originb.inkwisenote2.functionalUtils.Try;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteWithImage;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote2.modules.repositories.*;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TextParsingWorker extends Worker {
    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private final AtomicNotesDomain atomicNotesDomain;

    private final AppConfig appConfig;
    private final AppSecrets appSecrets;
    private final NoteOcrTextDao noteOcrTextDao;
    private final Logger logger = new Logger("TextParsingWorker");
    private DigitalInkRecognizer recognizer;

    @AllArgsConstructor
    private class ParsingInput {
        public long bookId;
        public long noteId;
    }

    public TextParsingWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
        this.atomicNotesDomain = Repositories.getInstance().getAtomicNotesDomain();
        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();
        appConfig = ConfigReader.getInstance().getAppConfig();
        appSecrets = appConfig.getAppSecrets();

        initializeRecognizer();
    }

    private void initializeRecognizer() {
        try {
            // Initialize ML Kit recognizer with English model
            String languageTag = "en-US"; // Default to English
            DigitalInkRecognitionModelIdentifier modelIdentifier =
                    DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag);

            if (modelIdentifier == null) {
                logger.error("Model for language " + languageTag + " not found");
                return;
            }

            DigitalInkRecognitionModel model =
                    DigitalInkRecognitionModel.builder(modelIdentifier).build();

            // Download model if not already available
            RemoteModelManager modelManager = RemoteModelManager.getInstance();
            modelManager.download(model, new DownloadConditions.Builder().build())
                    .addOnSuccessListener(unused ->
                            logger.debug("Model downloaded successfully"))
                    .addOnFailureListener(e ->
                            logger.error("Error downloading model: " + e.getMessage()));

            // Create recognizer
            recognizer = DigitalInkRecognition.getClient(
                    DigitalInkRecognizerOptions.builder(model).build());
        } catch (Exception e) {
            logger.error("Failed to initialize ML Kit: " + e.getMessage());
        }
    }

    @NotNull
    @Override
    public Result doWork() {
        Try.to(() -> {
                    long bookId = getInputData().getLong("book_id", -1);
                    long noteId = getInputData().getLong("note_id", -1);
                    return new ParsingInput(bookId, noteId);
                }, logger)
                .get()
                .filter(this::isNoteIdGreaterThan0)
                .ifPresent(this::parseTextForNotebook);

        return Result.success();
    }

    public void parseTextForNotebook(ParsingInput input) {
        long bookId = input.bookId;
        long noteId = input.noteId;
        logger.debug("Parsing text from a notebook (new flow): " + bookId);

        AtomicNoteEntity atomicNote = atomicNotesDomain.getAtomicNote(noteId);
        if (atomicNote == null) return;

        HandwrittenNoteWithImage handwrittenNoteWithImage = handwrittenNoteRepository.getNoteImage(atomicNote, BitmapScale.FULL_SIZE);
        HandwrittenNoteEntity handwrittenNoteEntity = handwrittenNoteWithImage.handwrittenNoteEntity;

        NoteOcrText noteOcrTextDb = noteOcrTextDao.readTextFromDb(atomicNote.getNoteId());
        NoteOcrText noteOcrText;
        if (ConfigReader.isAzureOcrEnabled()) {
           noteOcrText = parseTextForHandwrittenNoteWithAzure(atomicNote,
                   handwrittenNoteWithImage,
                    noteOcrTextDb);
        } else {
            noteOcrText = parseTextForHandwrittenNote(atomicNote,
                    handwrittenNoteEntity,
                    noteOcrTextDb);
        }

        if (noteOcrText == null) {
            onFailure(bookId, noteId);
        }

        if (noteOcrTextDb == null) {
            logger.debug("Inserting extracted text", ListUtils.listOf(noteOcrText, handwrittenNoteEntity));
            noteOcrTextDao.insertTextToDb(noteOcrText);
        } else {
            logger.debug("Updating extracted text", ListUtils.listOf(noteOcrText, handwrittenNoteEntity));
            noteOcrTextDao.updateTextToDb(noteOcrText);
        }

        logger.debug("Scheduling text processing work for book: " + bookId);
        WorkManagerBus.scheduleWorkForTextProcessingForBook(getApplicationContext(), bookId, noteId);
    }

    public NoteOcrText parseTextForHandwrittenNote(AtomicNoteEntity atomicNote,
                                                   HandwrittenNoteEntity handwrittenNoteEntity,
                                                   NoteOcrText noteOcrTextDb) {

        // Check if note has changed since last OCR
        if (noteOcrTextDb != null && noteOcrTextDb.getNoteHash().equals(handwrittenNoteEntity.getBitmapHash())) {
            logger.debug("Note ocr has the latest text, skipping", handwrittenNoteEntity);
            return noteOcrTextDb;
        }

        // Get strokes from the note
        List<Stroke> strokes = handwrittenNoteRepository.getStrokes(atomicNote.getNoteId());

        if (strokes == null || strokes.isEmpty()) {
            logger.error("No strokes found for note", handwrittenNoteEntity);
            return null;
        }

        // Use ML Kit to recognize text from strokes
        Optional<String> recognizedText = recognizeInkText(strokes);

        if (!recognizedText.isPresent()) {
            logger.error("Failed to recognize text using ML Kit", handwrittenNoteEntity);
            return null;
        }

        // Save recognized text
        NoteOcrText noteOcrText = new NoteOcrText(
                atomicNote.getNoteId(),
                handwrittenNoteEntity.getBitmapHash(),
                recognizedText.get()
        );
        return noteOcrText;
    }

    private Optional<String> recognizeInkText(List<Stroke> appStrokes) {
        if (recognizer == null) {
            logger.error("Recognizer not initialized");
            return Optional.empty();
        }

        try {
            // Convert app strokes to ML Kit ink format
            Ink.Builder inkBuilder = Ink.builder();

            for (Stroke appStroke : appStrokes) {
                Ink.Stroke.Builder strokeBuilder = Ink.Stroke.builder();

                for (StrokePoint appPoint : appStroke.getPoints()) {
                    strokeBuilder.addPoint(
                            Ink.Point.create(appPoint.getX(),
                                    appPoint.getY(),
                                    appPoint.getTimestamp()));
                }

                inkBuilder.addStroke(strokeBuilder.build());
            }

            Ink ink = inkBuilder.build();

            // CompletableFuture to handle async recognition
            CompletableFuture<String> future = new CompletableFuture<>();

            // Recognize text
            recognizer.recognize(ink)
                    .addOnSuccessListener(result -> {
                        String text = "";
                        if (!result.getCandidates().isEmpty()) {
                            text = result.getCandidates().get(0).getText();
                        }
                        future.complete(text);
                    })
                    .addOnFailureListener(e -> {
                        logger.error("Recognition failed: " + e.getMessage());
                        future.completeExceptionally(e);
                    });

            // Wait for result
            return Optional.of(future.get());
        } catch (Exception e) {
            logger.error("Error during ink recognition: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Result onFailure(Long bookId, long noteId) {
        WorkManagerBus.scheduleWorkForFindingRelatedNotesForBook(getApplicationContext(), bookId, noteId);
        return ListenableWorker.Result.failure();
    }

    private boolean isNoteIdGreaterThan0(ParsingInput parsingInput) {
        if (parsingInput.bookId == -1 || parsingInput.noteId == -1) {
            logger.error("Got incorrect note id (-1) as input: bookId"
                    + parsingInput.bookId + " noteId: " + parsingInput.noteId);
            return false;
        }
        return true;
    }

    public NoteOcrText parseTextForHandwrittenNoteWithAzure(AtomicNoteEntity atomicNote,
                                                            HandwrittenNoteWithImage handwrittenNoteWithImage,
                                                            NoteOcrText noteOcrTextDb) {
        HandwrittenNoteEntity handwrittenNoteEntity = handwrittenNoteWithImage.handwrittenNoteEntity;
        Optional<Bitmap> bitmapOpt = handwrittenNoteWithImage.noteImage;
        if (!bitmapOpt.isPresent()) {
            logger.error("Handwritten note doesn't have image", handwrittenNoteEntity);
            return null;
        }
        Bitmap noteBitmap = bitmapOpt.get();
        if (noteOcrTextDb != null && noteOcrTextDb.getNoteHash().equals(handwrittenNoteEntity.getBitmapHash())) {
            logger.debug("Note ocr has the latest text, skipping", handwrittenNoteEntity);
            return noteOcrTextDb;
        }

        Optional<AzureOcrResult> azureResultOpt = applyAzureOcr(noteBitmap);
        if (!azureResultOpt.isPresent()) {
            logger.error("Failed to get text using OCR", handwrittenNoteEntity);
            return null;
        }
        NoteOcrText noteOcrText = new NoteOcrText(atomicNote.getNoteId(),
                handwrittenNoteEntity.getBitmapHash(),
                azureResultOpt.get().readResult.content);
        return noteOcrText;
    }

    private Optional<AzureOcrResult> applyAzureOcr(Bitmap bitmap) {
        logger.debug("got bitmap");
        Optional<AzureOcrResult> ocrResult = Try.to(() -> {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    // Compress the bitmap into the ByteArrayOutputStream
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                    // Convert the ByteArrayOutputStream to an InputStream
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    InputStream imageStream = new ByteArrayInputStream(byteArray);
                    OcrService.AnalyzeImageTask imageTask = new OcrService.AnalyzeImageTask(appSecrets);
                    AzureOcrResult azureOcrResult = imageTask.runOcr(imageStream);

                    logger.debug("Ocr result: " + azureOcrResult);

                    return azureOcrResult;
                }, new Logger("TextParsingJob"))
                .logIfError("Failed to convert handwriting to text")
                .get();
        return ocrResult;
    }
}
