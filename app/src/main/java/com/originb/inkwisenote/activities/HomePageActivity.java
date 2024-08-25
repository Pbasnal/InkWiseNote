package com.originb.inkwisenote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote.noterepository.NoteRepository;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.adapters.NoteGridAdapter;

public class HomePageActivity extends AppCompatActivity {
    private NoteRepository noteRepository;
    private RecyclerView recyclerView;
    private NoteGridAdapter noteGridAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        noteRepository = new NoteRepository(getFilesDir());

        createGridLayoutToShowNotes();
        createNewNoteButton();
    }

    public void createGridLayoutToShowNotes() {
        recyclerView = findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        noteGridAdapter = new NoteGridAdapter(noteRepository.listNoteNamesInDirectory(),
                this,
                noteRepository);

        recyclerView.setAdapter(noteGridAdapter);
        recyclerView.setHasFixedSize(true);
    }

    public void createNewNoteButton() {
        FloatingActionButton fab = findViewById(R.id.fab_add_note);
        fab.setOnClickListener(onNewNoteTapCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the note list on resume
        noteGridAdapter.updateNotes(noteRepository.listNoteNamesInDirectory());
    }

    public View.OnClickListener onNewNoteTapCallback = v -> {
        // Start NoteActivity to create a new note
        Intent intent = new Intent(HomePageActivity.this, NoteActivity.class);
        startActivity(intent);
    };

}
