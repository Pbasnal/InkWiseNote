package com.originb.inkwisenote.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.BaseColumns;
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
import com.originb.inkwisenote.data.repositories.NoteRepository;
import com.originb.inkwisenote.functionalUtils.Try;
import com.originb.inkwisenote.io.ocr.OcrService;
//import com.originb.inkwisenote.io.ocr.TesseractsOcr;
import com.originb.inkwisenote.io.sql.NoteTextContract;
import com.originb.inkwisenote.modules.Repositories;
import com.originb.inkwisenote.views.DrawingView;
import com.originb.inkwisenote.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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

        drawingView = findViewById(R.id.drawing_view);

//        tesseractsOcr = Repositories.getInstance().getTesseractsOcr();
        noteTextDbHelper = Repositories.getInstance().getNoteTextDbHelper();
        noteRepository = new NoteRepository();
        appSecrets = ConfigReader.fromContext(this).getAppConfig().getAppSecrets();
        noteStack = new NoteStack(noteRepository);
        debugContext = new DebugContext("NoteActivity");

        noteTitleField = findViewById(R.id.note_title);
        newNoteButton = findViewById(R.id.fab_add_note);
        prevNoteButton = findViewById(R.id.fab_prev_note);
        nextNoteButton = findViewById(R.id.fab_next_note);
        createdTime = findViewById(R.id.note_created_time);
        ocrResult = findViewById(R.id.ocr_result);


        workingNotePath = getIntent().getStringExtra("workingNotePath");
        Long noteIdToOpen = getIntent().getLongExtra("noteId", 0);
        Optional<NoteEntity> noteEntityOpt = noteRepository.getNoteEntity(noteIdToOpen);
        if (!noteEntityOpt.isPresent()) {
            noteEntityOpt = noteRepository.saveNote(workingNotePath,
                    "",
                    drawingView.getNewBitmap(),
                    drawingView.getNewPageTemplate());
        }

        noteEntityOpt.ifPresent(noteStack::setCurrentNote);
        noteEntityOpt = noteEntityOpt.flatMap(this::renderNote);
        if (!noteEntityOpt.isPresent()) {
            Log.e(debugContext.getDebugInfo(), "Failed to load note");
            Toast.makeText(this, "Failed to load note", Toast.LENGTH_SHORT).show();
            finish();
        }


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

        nextNoteButton.setOnClickListener(v -> {
            saveCurrentNote();

            Optional<NoteEntity> nextNoteEntityOpt = noteStack.moveToNextNote();

            nextNoteEntityOpt.ifPresent(this::renderNote);
        });

        prevNoteButton.setOnClickListener(v -> {
            saveCurrentNote();
            Optional<NoteEntity> prevNoteEntityOpt = noteStack.moveToPrevNote();

            prevNoteEntityOpt.ifPresent(noteEntity -> {
                renderNote(noteEntity);
                createdTime.setText(getCreateDateTime(noteEntity));
            });
        });
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

    private void setActivityNoteTitle(NoteMeta noteMeta) {
        if (Objects.nonNull(noteMeta.getNoteTitle())) {
            noteTitleField.setText(noteMeta.getNoteTitle());
        } else {
            noteTitleField.setText("");
        }
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
        noteEntityOpt.ifPresent(noteEntity -> applyOcr(noteEntity.getNoteMeta(), this::updateNoteMeta));
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

    private void applyOcr(NoteMeta noteMeta, Consumer<NoteMeta> callback) {
        Try.to(() -> {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    // Compress the bitmap into the ByteArrayOutputStream
                    drawingView.getBitmap()
                            .compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                    // Convert the ByteArrayOutputStream to an InputStream
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    InputStream imageStream = new ByteArrayInputStream(byteArray);
                    OcrService.convertHandwritingToText(imageStream, appSecrets, result -> {
                        Log.d("NoteActivity", "Ocr result: " + result);
                        noteMeta.setAzureOcrResult(result);
                        noteMeta.setExtractedText(result.readResult.content);

                        List<Long> noteIds = NoteTextContract.NoteTextQueries.readTextFromDb(noteMeta, noteTextDbHelper);
                        if (CollectionUtils.isEmpty(noteIds)) {
                            NoteTextContract.NoteTextQueries.insertTextToDb(noteMeta, noteTextDbHelper);
                        } else {
                            NoteTextContract.NoteTextQueries.updateTextToDb(noteMeta, noteTextDbHelper);
                        }
                        Toast.makeText(this, "Generated text from note", Toast.LENGTH_SHORT).show();
                        callback.accept(noteMeta);
                    });
                }, debugContext)
                .logIfError("Failed to convert handwriting to text")
                .get();
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
