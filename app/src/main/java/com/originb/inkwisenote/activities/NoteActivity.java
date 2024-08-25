package com.originb.inkwisenote.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.originb.inkwisenote.DrawingView;
import com.originb.inkwisenote.Note;
import com.originb.inkwisenote.NoteRepository;
import com.originb.inkwisenote.R;

public class NoteActivity extends AppCompatActivity {
    private DrawingView drawingView;
    private NoteRepository noteRepository;
    private Note note;
    private String noteName;
    private String noteBitmapName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        drawingView = findViewById(R.id.drawing_view);
        noteRepository = new NoteRepository(getFilesDir());

        noteName = getIntent().getStringExtra("noteFileName");
        noteBitmapName = noteName;
        if (noteName != null) {
            try {
                note = noteRepository.loadNoteFromDisk(noteName);
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

        if (note.getNoteName() == null) {
            noteName = "note_" + System.currentTimeMillis();
            note.setNoteName(noteName);
            note.setBitmapName(noteName);
        }
        try {
            noteRepository.saveNoteToDisk(note, drawingView.bitmap);
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
