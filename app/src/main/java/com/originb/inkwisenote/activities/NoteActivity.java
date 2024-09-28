package com.originb.inkwisenote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.data.NoteEntity;
import com.originb.inkwisenote.data.NoteMeta;
import com.originb.inkwisenote.data.repositories.NoteRepository;
import com.originb.inkwisenote.functionalUtils.Try;
import com.originb.inkwisenote.views.DrawingView;
import com.originb.inkwisenote.R;

import java.util.Objects;
import java.util.Optional;

public class NoteActivity extends AppCompatActivity {

    private DebugContext debugContext;

    private NoteRepository noteRepository;

    private DrawingView drawingView;

    private NoteStack noteStack;

    private String workingNotePath;

    private EditText noteTitleField;
    private FloatingActionButton newNoteButton;
    private FloatingActionButton prevNoteButton;
    private FloatingActionButton nextNoteButton;

    private boolean isSaved = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        drawingView = findViewById(R.id.drawing_view);

        noteRepository = new NoteRepository();
        noteStack = new NoteStack(noteRepository);
        debugContext = new DebugContext("NoteActivity");

        noteTitleField = findViewById(R.id.note_title);
        newNoteButton = findViewById(R.id.fab_add_note);
        prevNoteButton = findViewById(R.id.fab_prev_note);
        nextNoteButton = findViewById(R.id.fab_next_note);

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

//
//            Optional<NoteMeta> currentNoteMetaOpt = Try.to(() -> noteStack.pop(), debugContext).get();
//            Optional<NoteMeta> prevNoteMetaOpt = Try.to(() -> noteStack.peek(), debugContext).get();
//            if (!prevNoteMetaOpt.isPresent() && currentNoteMetaOpt.isPresent()) {
//                Optional<NoteEntity> prevNoteOpt = noteRepository.getPrevNote(currentNoteMetaOpt.get().getNoteId());
//                prevNoteOpt.ifPresent(note -> noteStack.setCurrentNote(note.getNoteMeta()));
//            }
//            if (noteStack.isEmpty()) return;
//
//            NoteMeta prevNote = noteStack.peek();
            Optional<NoteEntity> prevNoteEntityOpt = noteStack.moveToPrevNote();

            prevNoteEntityOpt.ifPresent(noteEntity -> {
                renderNote(noteEntity);
                setVisibilityOfButtons(noteEntity.getNoteMeta());
            });
        });
    }

    private Optional<NoteEntity> renderNote(NoteEntity noteEntity) {
        return Try.to(() -> {
                    setActivityNoteTitle(noteEntity.getNoteMeta());
                    drawingView.setBitmap(noteEntity.getNoteBitmap());
                    drawingView.setPageTemplate(noteEntity.getPageTemplate());
                    setVisibilityOfButtons(noteEntity.getNoteMeta());

                    return noteEntity;
                }, debugContext)
                .logIfError("Failed to load note " + noteEntity.getNoteId())
                .get();
    }

    private void setVisibilityOfButtons(NoteMeta noteMeta) {
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

        Try.to(() -> noteRepository.updateNote(noteEntityOpt.get().getNoteMeta(),
                                drawingView.getBitmap(),
                                drawingView.getPageTemplate())
                        , debugContext)
                .logIfError("Failed to save note " + noteEntityOpt)
                .get();
    }
}
