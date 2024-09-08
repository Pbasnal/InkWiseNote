package com.originb.inkwisenote.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import com.originb.inkwisenote.DrawingView;
import com.originb.inkwisenote.data.Note;
import com.originb.inkwisenote.filemanager.BitmapFileManager;
import com.originb.inkwisenote.filemanager.FileInfo;
import com.originb.inkwisenote.filemanager.FileType;
import com.originb.inkwisenote.filemanager.JsonFileManager;
import com.originb.inkwisenote.repositories.NoteRepository;
import com.originb.inkwisenote.R;

public class NoteActivity extends AppCompatActivity {
    private DrawingView drawingView;
    private NoteRepository noteRepository;
    private Note note;
    private String noteName;

    private boolean isSaved = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        drawingView = findViewById(R.id.drawing_view);
        noteRepository = new NoteRepository(getFilesDir());

        noteName = getIntent().getStringExtra("noteFileName");
        if (noteName != null) {
            try {
                noteRepository.getNoteFilesToLoad(noteName).forEach(fileInfo ->
                {
                    if (FileType.NOTE.equals(fileInfo.fileType)) {
                        note = JsonFileManager.<Note>readDataFromDisk(fileInfo).data;
                    } else {
                        FileInfo<Bitmap> bitmapInfo = BitmapFileManager.<Bitmap>readDataFromDisk(fileInfo);
                        drawingView.bitmap = bitmapInfo.data;
                        drawingView.setPaths(note.getPaths(), note.getPaints());
                    }
                });
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

    private void saveNote() {
        note.setNoteData(drawingView.getPaths(), drawingView.getPaints());

        if (note.getNoteName() == null) {
            noteName = "note_" + System.currentTimeMillis();
            note.setNoteName(noteName);
            note.setBitmapName(noteName);
        }
        try {
            noteRepository.getNoteFilesToSave(note, drawingView.bitmap)
                    .forEach(fileInfo ->
                    {
                        if (FileType.NOTE.equals(fileInfo.fileType)) {
                            JsonFileManager.writeDataToDisk(fileInfo);
                        } else BitmapFileManager.writeDataToDisk(fileInfo);
                    });
            // TODO: launch a callback from here to update the list of notes with the saved information
            // This will be need to updated the thumbnail if user saves by pressing back button
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
