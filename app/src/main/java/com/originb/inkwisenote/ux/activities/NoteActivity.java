package com.originb.inkwisenote.ux.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.config.AppSecrets;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.data.config.AppState;
import com.originb.inkwisenote.data.dao.NoteOcrTextDao;
import com.originb.inkwisenote.data.dao.NoteTaskStatusDao;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskName;
import com.originb.inkwisenote.data.entities.tasks.TfIdfRelationTasks;
import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.data.notedata.NoteMeta;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStatus;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStage;
import com.originb.inkwisenote.modules.backgroundworkers.WorkManagerBus;
import com.originb.inkwisenote.modules.messaging.BackgroundOps;
import com.originb.inkwisenote.modules.noteoperations.NoteOperations;
import com.originb.inkwisenote.ux.utils.NoteStack;
import com.originb.inkwisenote.modules.repositories.NoteRepository;
import com.originb.inkwisenote.modules.functionalUtils.Try;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.ux.views.DrawingView;
import com.originb.inkwisenote.R;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NoteActivity extends AppCompatActivity {

    private DebugContext debugContext;

    private NoteRepository noteRepository;
    //    private TesseractsOcr tesseractsOcr;
    private NoteOcrTextDao noteOcrTextDao;
    private NoteTaskStatusDao noteTaskStatusDao;
    private AppSecrets appSecrets;
    private NoteOperations noteOperations;

    private DrawingView drawingView;

    private NoteStack noteStack;

    private String workingNotePath;

    private EditText noteTitleField;
    private FloatingActionButton newNoteButton;
    private FloatingActionButton prevNoteButton;
    private FloatingActionButton nextNoteButton;
    private TextView createdTime;
//    private TextView ocrResult;

    private boolean isSaved = false;

    private boolean isPalmRejectionEnabled = true;  // Add toggle if needed
    private static final float PALM_PRESSURE_THRESHOLD = 0.3f;  // Adjust based on testing
    private static final int TOUCH_SLOP = 8;  // Minimum movement to consider it a deliberate touch

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // Activity Arguments
        workingNotePath = getIntent().getStringExtra("workingNotePath"); // TODO: validation?
        Long noteIdToOpen = getIntent().getLongExtra("noteId", 0);

        drawingView = findViewById(R.id.drawing_view);

//        tesseractsOcr = Repositories.getInstance().getTesseractsOcr();
        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();
        noteTaskStatusDao = Repositories.getInstance().getNotesDb().noteTaskStatusDao();
        noteRepository = new NoteRepository();
        appSecrets = ConfigReader.fromContext(this).getAppConfig().getAppSecrets();
        noteStack = new NoteStack(noteRepository);
        debugContext = new DebugContext("NoteActivity");
        noteOperations = new NoteOperations(this);

        noteTitleField = findViewById(R.id.note_title);
        createdTime = findViewById(R.id.note_created_time);
//        ocrResult = findViewById(R.id.ocr_result);

        setNewNoteButton();
        setPrevNoteButton();
        setNextNoteButton();

        Optional<NoteEntity> noteEntityOpt = initializeNewNoteIfEmpty(noteRepository.getNoteEntity(noteIdToOpen));

        noteEntityOpt.ifPresent(noteEntity -> {
            noteStack.setCurrentNote(noteEntity);
            renderNote(noteEntity);
        });

        if (!noteEntityOpt.isPresent()) {
            Log.e(debugContext.getDebugInfo(), "Failed to load/initialize note");
            Toast.makeText(this, "Failed to load note or initialize note", Toast.LENGTH_SHORT).show();
            finish();
        }
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

                saveCurrentNote();
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
                            .map(result -> result.content);
//                            .ifPresent(ocrResult::setText);

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

    private void saveCurrentNote() {
        String noteTitle = noteTitleField.getText().toString();
        Optional<NoteEntity> noteEntityOpt = noteStack.getCurrentNoteEntity();

        if (!noteEntityOpt.isPresent()) return;

        NoteEntity noteEntity = noteEntityOpt.get();
        noteEntity.getNoteMeta().setNoteTitle(noteTitle);

        noteOperations.updateNote(noteEntity, drawingView.getBitmap(), drawingView.getPageTemplate());
    }

//    private void saveNoteFiles(NoteEntity noteEntity) {
//        Try.to(() -> noteRepository.updateNote(noteEntity.getNoteMeta(),
//                                drawingView.getBitmap(),
//                                drawingView.getPageTemplate())
//                        , debugContext)
//                .logIfError("Failed to save note " + noteEntity)
//                .get();
//    }
//
//    private void updateNoteMeta(NoteMeta noteMeta) {
//        Try.to(() -> noteRepository.updateNoteMeta(noteMeta)
//                        , debugContext)
//                .logIfError("Failed to update noteMeta " + noteMeta)
//                .get();
//    }

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
