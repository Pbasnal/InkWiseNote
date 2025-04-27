package com.originb.inkwisenote2.modules.ocr.worker;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.config.AppSecrets;
import com.originb.inkwisenote2.config.ConfigReader;
import com.originb.inkwisenote2.common.BitmapScale;
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.ocr.data.AzureOcrResult;
import com.originb.inkwisenote2.modules.ocr.data.OcrService;
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
import java.util.HashSet;
import java.util.Optional;

public class TextParsingWorker extends Worker {
    private final HandwrittenNoteRepository handwrittenNoteRepository;

    //    private final SmartNotebookRepository smartNotebookRepository;
    private final AtomicNotesDomain atomicNotesDomain;

    private final AppSecrets appSecrets;
    private final NoteOcrTextDao noteOcrTextDao;

    private final Logger logger = new Logger("TextParsingWorker");

    public TextParsingWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
//        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        this.handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
        this.atomicNotesDomain = Repositories.getInstance().getAtomicNotesDomain();

        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();

        appSecrets = ConfigReader.getInstance().getAppConfig().getAppSecrets();
    }

    @AllArgsConstructor
    private class ParsingInput {
        public long bookId;
        public long noteId;
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

//        logger.debug("Notebook bookId: " + bookId + " contains number of notes: " + smartNotebook.getAtomicNotes().size());
//        for (AtomicNoteEntity atomicNote : smartNotebook.getAtomicNotes()) {
        if (!parseTextForHandwrittenNote(atomicNote)) {
            onFailure(bookId, noteId);
        }
//        }

        logger.debug("Scheduling text processing work for book: " + bookId);

        WorkManagerBus.scheduleWorkForTextProcessingForBook(getApplicationContext(), bookId);
    }

    public boolean parseTextForHandwrittenNote(AtomicNoteEntity atomicNote) {
        HandwrittenNoteWithImage handwrittenNoteWithImage = handwrittenNoteRepository.getNoteImage(atomicNote, BitmapScale.FULL_SIZE);
        HandwrittenNoteEntity handwrittenNoteEntity = handwrittenNoteWithImage.handwrittenNoteEntity;
        Optional<Bitmap> bitmapOpt = handwrittenNoteWithImage.noteImage;
        if (!bitmapOpt.isPresent()) {
            logger.error("Handwritten note doesn't have image", handwrittenNoteEntity);
            return false;
        }
        Bitmap noteBitmap = bitmapOpt.get();
        NoteOcrText noteOcrTextDb = noteOcrTextDao.readTextFromDb(atomicNote.getNoteId());
        if (noteOcrTextDb != null && noteOcrTextDb.getNoteHash().equals(handwrittenNoteEntity.getBitmapHash())) {
            logger.debug("Note ocr has the latest text, skipping", handwrittenNoteEntity);
            return true;
        }

        Optional<AzureOcrResult> azureResultOpt = applyAzureOcr(noteBitmap);
        if (!azureResultOpt.isPresent()) {
            logger.error("Failed to get text using OCR", handwrittenNoteEntity);
            return false;
        }
        NoteOcrText noteOcrText = new NoteOcrText(atomicNote.getNoteId(),
                handwrittenNoteEntity.getBitmapHash(),
                azureResultOpt.get().readResult.content);

        if (noteOcrTextDb == null) {
            logger.debug("Inserting extracted text", ListUtils.listOf(noteOcrText, handwrittenNoteEntity));
            noteOcrTextDao.insertTextToDb(noteOcrText);
        } else {
            logger.debug("Updating extracted text", ListUtils.listOf(noteOcrText, handwrittenNoteEntity));
            noteOcrTextDao.updateTextToDb(noteOcrText);
        }
        return true;
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
                }, new Logger("TextParsingJob"))
                .logIfError("Failed to convert handwriting to text")
                .get();
        return ocrResult;
    }
}
