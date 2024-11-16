package com.originb.inkwisenote.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.config.AppSecrets;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.data.NoteEntity;
import com.originb.inkwisenote.data.NoteMeta;
import com.originb.inkwisenote.data.NoteOcrText;
import com.originb.inkwisenote.data.repositories.NoteRepository;
import com.originb.inkwisenote.functionalUtils.Try;
import com.originb.inkwisenote.io.ocr.AzureOcrResult;
import com.originb.inkwisenote.io.ocr.OcrService;
import com.originb.inkwisenote.io.sql.NoteTextContract;
import com.originb.inkwisenote.modules.Repositories;
import com.originb.inkwisenote.views.DrawingView;
import com.originb.inkwisenote.R;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NoteActivity extends AppCompatActivity {

    private DebugContext debugContext;

    private NoteRepository noteRepository;
    //    private TesseractsOcr tesseractsOcr;
    private NoteTextContract.NoteTextDbHelper noteTextDbHelper;
    private AppSecrets appSecrets;

    private DrawingView drawingView;

    private NoteStack noteStack;

    private String workingNotePath;

    private EditText noteTitleField;
    private FloatingActionButton newNoteButton;
    private FloatingActionButton prevNoteButton;
    private FloatingActionButton nextNoteButton;
    private TextView createdTime;
    private TextView ocrResult;

    private boolean isSaved = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // Activity Arguments
        workingNotePath = getIntent().getStringExtra("workingNotePath"); // TODO: validation?
        Long noteIdToOpen = getIntent().getLongExtra("noteId", 0);

        drawingView = findViewById(R.id.drawing_view);

//        tesseractsOcr = Repositories.getInstance().getTesseractsOcr();
        noteTextDbHelper = Repositories.getInstance().getNoteTextDbHelper();
        noteRepository = new NoteRepository();
        appSecrets = ConfigReader.fromContext(this).getAppConfig().getAppSecrets();
        noteStack = new NoteStack(noteRepository);
        debugContext = new DebugContext("NoteActivity");

        noteTitleField = findViewById(R.id.note_title);
        createdTime = findViewById(R.id.note_created_time);
        ocrResult = findViewById(R.id.ocr_result);

        setNewNoteButton();
        setPrevNoteButton();
        setNextNoteButton();

        Optional<NoteEntity> noteEntityOpt = initializeNewNoteIfEmpty(noteRepository.getNoteEntity(noteIdToOpen));
        if (!noteEntityOpt.isPresent()) {
            Log.e(debugContext.getDebugInfo(), "Failed to load/initialize note");
            Toast.makeText(this, "Failed to load note or initialize note", Toast.LENGTH_SHORT).show();
            finish();
        }

        noteEntityOpt.ifPresent(noteStack::setCurrentNote);
        noteEntityOpt.flatMap(this::renderNote);
    }

    private Optional<NoteEntity> initializeNewNoteIfEmpty(Optional<NoteEntity> noteEntityOpt) {
        if (!noteEntityOpt.isPresent()) {
            return noteRepository.saveNote(workingNotePath,
                    "",
                    drawingView.getNewBitmap(),
                    drawingView.getNewPageTemplate());
        }
        return noteEntityOpt;
    }

    private void setNextNoteButton() {
        nextNoteButton = findViewById(R.id.fab_next_note);
        nextNoteButton.setOnClickListener(v -> {
            saveCurrentNote();

            Optional<NoteEntity> nextNoteEntityOpt = noteStack.moveToNextNote();

            nextNoteEntityOpt.ifPresent(this::renderNote);
        });
    }

    private void setPrevNoteButton() {
        prevNoteButton = findViewById(R.id.fab_prev_note);
        prevNoteButton.setOnClickListener(v -> {
            saveCurrentNote();
            Optional<NoteEntity> prevNoteEntityOpt = noteStack.moveToPrevNote();

            prevNoteEntityOpt.ifPresent(noteEntity -> {
                renderNote(noteEntity);
                createdTime.setText(getCreateDateTime(noteEntity));
            });
        });
    }

    private void setNewNoteButton() {
        newNoteButton = findViewById(R.id.fab_add_note);
        newNoteButton.setOnClickListener(v -> {
            saveCurrentNote();

            Optional<NoteEntity> newNoteEntityOpt = noteRepository.saveNote(workingNotePath,
                    "",
                    drawingView.getNewBitmap(),
                    drawingView.getNewPageTemplate());

            Optional<NoteEntity> currentNoteEntity = noteStack.getCurrentNoteEntity();
            if (newNoteEntityOpt.isPresent() && currentNoteEntity.isPresent()) {
                NoteMeta newNoteMeta = newNoteEntityOpt.get().getNoteMeta();
                NoteMeta currentNoteMeta = currentNoteEntity.get().getNoteMeta();

                currentNoteMeta.getNextNoteIds().add(newNoteMeta.getNoteId());
                newNoteMeta.getPrevNoteIds().add(currentNoteMeta.getNoteId());

                noteStack.setCurrentNote(newNoteEntityOpt.get());
                renderNote(newNoteEntityOpt.get());
            }
        });
    }

    private void setActivityNoteTitle(NoteMeta noteMeta) {
        if (Objects.nonNull(noteMeta.getNoteTitle())) {
            noteTitleField.setText(noteMeta.getNoteTitle());
        } else {
            noteTitleField.setText("");
        }
    }

    private void setVisibilityOfButtons() {
        newNoteButton.setVisibility(FloatingActionButton.VISIBLE);

        prevNoteButton.setVisibility(FloatingActionButton.INVISIBLE);
        if (noteStack.hasPreviousNote()) {
            prevNoteButton.setVisibility(FloatingActionButton.VISIBLE);
        }

        nextNoteButton.setVisibility(FloatingActionButton.INVISIBLE);
        if (noteStack.hasNextNote()) {
            nextNoteButton.setVisibility(FloatingActionButton.VISIBLE);
        }
    }

    private Optional<NoteEntity> renderNote(NoteEntity noteEntity) {
        return Try.to(() -> {
                    setActivityNoteTitle(noteEntity.getNoteMeta());

                    drawingView.setBitmap(noteEntity.getNoteBitmap());
                    drawingView.setPageTemplate(noteEntity.getPageTemplate());

                    setVisibilityOfButtons();
                    createdTime.setText(getCreateDateTime(noteEntity));

                    Optional.ofNullable(noteEntity.getNoteMeta().getAzureOcrResult())
                            .map(azureOcrResult -> azureOcrResult.readResult)
                            .map(result -> result.content)
                            .ifPresent(ocrResult::setText);

                    return noteEntity;
                }, debugContext)
                .logIfError("Failed to load note " + noteEntity.getNoteId())
                .get();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (isSaved) {
            isSaved = false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isSaved) {
            saveCurrentNote();
            isSaved = true;
        }
    }

    public static void newNoteIntent(Intent intent, String currentDirectoryPath) {
        intent.putExtra("workingNotePath", currentDirectoryPath);
    }

    public static void openNoteIntent(Intent intent, String currentDirectoryPath, Long noteId, String noteFileName) {
        intent.putExtra("workingNotePath", currentDirectoryPath);
        intent.putExtra("noteId", noteId);
    }

    private void saveCurrentNote() {
        String noteTitle = noteTitleField.getText().toString();
        Optional<NoteEntity> noteEntityOpt = noteStack.getCurrentNoteEntity();
        noteEntityOpt.ifPresent(noteEntity -> noteEntity.getNoteMeta().setNoteTitle(noteTitle));

        noteEntityOpt.ifPresent(this::saveNoteFiles);
        Toast.makeText(this, "Analyzing notes", Toast.LENGTH_SHORT).show();
        noteEntityOpt.ifPresent(noteEntity -> {
            applyOcr(noteEntity.getNoteMeta());
            updateNoteMeta(noteEntity.getNoteMeta());
        });
    }

//    private void applyOcrWithTess(NoteMeta noteMeta, Function<NoteMeta, Void> callback) {
//        Try.to(() -> {
//                    Bitmap bitmap = drawingView.getBitmap();
//                    String imageText = tesseractsOcr.extractText(bitmap);
//                    noteMeta.setExtractedText(imageText);
////                    OcrService.convertHandwritingToText(imageStream, result -> {
//                    Log.d("NoteActivity", "Ocr result: " + imageText);
////                        noteMeta.setAzureOcrResult(imageText);
//                    callback.apply(noteMeta);
////                    });
//                }, debugContext)
//                .logIfError("Failed to convert handwriting to text")
//                .get();
//    }

    private Optional<NoteOcrText> applyOcr(NoteMeta noteMeta) {
       Optional<NoteOcrText> noteTextOpt = Try.to(() -> {
                    AzureOcrResult azureOcrResult = runOcrOnImage();
                    Log.d("NoteActivity", "Ocr result: " + azureOcrResult);
                    NoteOcrText noteOcrText = new NoteOcrText(noteMeta.getNoteId(), azureOcrResult.readResult.content);

                    List<NoteOcrText> noteOcrTexts = NoteTextContract.NoteTextQueries.readTextFromDb(noteOcrText.getNoteId(), noteTextDbHelper);
                    if (CollectionUtils.isEmpty(noteOcrTexts)) {
                        NoteTextContract.NoteTextQueries.insertTextToDb(noteOcrText, noteTextDbHelper);
                    } else {
                        NoteTextContract.NoteTextQueries.updateTextToDb(noteOcrText, noteTextDbHelper);
                    }
                    Toast.makeText(this, "Generated text from note", Toast.LENGTH_SHORT).show();

                    return noteOcrText;
                }, debugContext)
                .logIfError("Failed to convert handwriting to text")
                .get();
       return noteTextOpt;
    }

    @SneakyThrows
    private AzureOcrResult runOcrOnImage() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Compress the bitmap into the ByteArrayOutputStream
        drawingView.getBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        // Convert the ByteArrayOutputStream to an InputStream
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        InputStream imageStream = new ByteArrayInputStream(byteArray);
        return OcrService.convertHandwritingToText(imageStream, appSecrets).get();
    }

    private void saveNoteFiles(NoteEntity noteEntity) {
        Try.to(() -> noteRepository.updateNote(noteEntity.getNoteMeta(),
                                drawingView.getBitmap(),
                                drawingView.getPageTemplate())
                        , debugContext)
                .logIfError("Failed to save note " + noteEntity)
                .get();
    }

    private void updateNoteMeta(NoteMeta noteMeta) {
        Try.to(() -> noteRepository.updateNoteMeta(noteMeta)
                        , debugContext)
                .logIfError("Failed to update noteMeta " + noteMeta)
                .get();
    }

    private String getCreateDateTime(NoteEntity noteEntity) {

        Long timestamp = noteEntity.getNoteMeta().getCreatedTimeMillis();
        if (Objects.isNull(timestamp)) return "";
        LocalDateTime dateTime = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        // Define the date-time formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy HH:mm");

        return dateTime.format(formatter);
    }
}
