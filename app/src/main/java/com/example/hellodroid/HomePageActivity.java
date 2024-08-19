package com.example.hellodroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

public class HomePageActivity extends AppCompatActivity implements NoteGridAdapter.OnNoteClickListener {
    private NoteRepository noteRepository;
    private RecyclerView recyclerView;
    //    private NoteAdapter noteAdapter;
    private NoteGridAdapter noteGridAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        noteRepository = new NoteRepository(getFilesDir());

        recyclerView = findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        noteGridAdapter = new NoteGridAdapter(noteRepository.listNotes(), this);

//        noteAdapter = new NoteAdapter(noteRepository.listNotes(), this);
        recyclerView.setAdapter(noteGridAdapter);
        recyclerView.setHasFixedSize(true);

        FloatingActionButton fab = findViewById(R.id.fab_add_note);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start NoteActivity to create a new note
                Intent intent = new Intent(HomePageActivity.this, NoteActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the note list on resume
        noteGridAdapter.updateNotes(noteRepository.listNotes());
    }

    @Override
    public void onNoteClick(File noteFile) {
        // Open NoteActivity with the selected note
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra("noteFileName", noteFile.getName());
        startActivity(intent);
    }
}
