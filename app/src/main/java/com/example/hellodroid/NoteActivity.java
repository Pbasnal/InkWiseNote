package com.example.hellodroid;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class NoteActivity extends AppCompatActivity {
    private DrawingView drawingView;
    private NoteRepository noteRepository;
    private Note note;
    private String noteFileName;
    private String noteBitmapName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        drawingView = findViewById(R.id.drawing_view);
        noteRepository = new NoteRepository(getFilesDir());

        noteFileName = getIntent().getStringExtra("noteFileName");
        noteBitmapName = noteFileName + ".png";
        if (noteFileName != null) {
            try {
                note = noteRepository.loadNoteFromDisk(noteFileName);
                drawingView.bitmap = noteRepository.loadBitmapFromDisk(noteBitmapName);
                drawingView.setPaths(note.getPaths(), note.getPaints());
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load note", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                note = new Note();
            }
        } else {
            note = new Note();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveNote();
    }

    private void saveNote() {
        note.setNoteData(drawingView.getPaths(), drawingView.getPaints());

        if (noteFileName == null) {
            noteFileName = "note_" + System.currentTimeMillis() + ".note";
            noteBitmapName = noteFileName + ".png";
        }
        try {
            noteRepository.saveNoteToDisk(note, noteFileName);
            noteRepository.saveBitmapToDisk(drawingView.bitmap, noteBitmapName);
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
