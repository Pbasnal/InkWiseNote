package com.originb.inkwisenote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.originb.inkwisenote.io.BitmapRepository;
import com.originb.inkwisenote.io.PageTemplateRepository;
import com.originb.inkwisenote.modules.Repositories;
import com.originb.inkwisenote.io.NoteRepository;
import com.originb.inkwisenote.views.DrawingView;
import com.originb.inkwisenote.data.Note;
import com.originb.inkwisenote.R;

import java.util.Objects;

public class NoteActivity extends AppCompatActivity {
    private NoteRepository noteRepository;
    private BitmapRepository bitmapRepository;
    private PageTemplateRepository pageTemplateRepository;

    private DrawingView drawingView;
    private Note note;

    private String noteName;
    private String workingNotePath;
    private Long noteId;

    private boolean isSaved = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        drawingView = findViewById(R.id.drawing_view);

        noteRepository = Repositories.getInstance().getNotesRepository();
        bitmapRepository = Repositories.getInstance().getBitmapRepository();
        pageTemplateRepository = Repositories.getInstance().getPageTemplateRepository();

        noteName = getIntent().getStringExtra("noteFileName");
        workingNotePath = getIntent().getStringExtra("workingNotePath");
        noteId = getIntent().getLongExtra("noteId", 0);
        if (isNewNote()) {
            note = NoteRepository.createNewNote();
        } else {
            try {
                noteRepository.getNote(noteId).ifPresent(this::setNote);
                bitmapRepository.getFullBitmap(noteId).ifPresent(drawingView::setBitmap);
                pageTemplateRepository.getPageTemplate(noteId).ifPresent(drawingView::setPageTemplate);

            } catch (Exception e) {
                Toast.makeText(this, "Failed to load note", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                if (Objects.isNull(note)) {
                    note = NoteRepository.createNewNote();
                }
            }
        }
    }

    private void setNote(Note note) {
        this.note = note;
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
            saveNote();
            isSaved = true;
        }
    }

    public static void newNoteIntent(Intent intent, String currentDirectoryPath) {
        intent.putExtra("workingNotePath", currentDirectoryPath);
    }

    public static void openNoteIntent(Intent intent, String currentDirectoryPath, Long noteId, String noteFileName) {
        intent.putExtra("noteFileName", noteFileName);
        intent.putExtra("workingNotePath", currentDirectoryPath);
        intent.putExtra("noteId", noteId);
    }


    private boolean isNewNote() {
        return Objects.isNull(noteName) || Objects.isNull(noteId);
    }

    private void saveNote() {
        try {
            if (isNewNote()) {
                note = noteRepository.createNewNote();
                noteRepository.saveNote(workingNotePath, noteId, note);
                bitmapRepository.saveBitmap(noteId,
                        noteRepository.getPathOfNote(noteId),
                        note.getNoteName(),
                        drawingView.getBitmap());
                noteName = note.getNoteName();
            } else {
                noteRepository.updateNoteMeta(noteId, note);
                bitmapRepository.updateBitmap(noteId, drawingView.getBitmap());
            }
            pageTemplateRepository.savePageTemplate(noteId,
                    noteRepository.getPathOfNote(noteId),
                    note.getNoteName(),
                    drawingView.getPageTemplate());
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
