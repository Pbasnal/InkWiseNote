package com.originb.inkwisenote.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.adapters.NoteGridAdapter;
import com.originb.inkwisenote.io.sql.NoteTextContract;
import com.originb.inkwisenote.modules.repositories.Repositories;

import java.util.ArrayList;
import java.util.List;

public class NoteSearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private Button searchButton;
    private List<Long> resultsList;

    private NoteTextContract.NoteTextDbHelper noteTextDbHelper;

    private RecyclerView recyclerView;
    private NoteGridAdapter noteGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        noteTextDbHelper = Repositories.getInstance().getNoteTextDbHelper();

        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);

        createGridLayoutToShowNotes();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSearch(searchInput.getText().toString());
            }
        });
    }

    public void createGridLayoutToShowNotes() {
        resultsList = new ArrayList<>();

        recyclerView = findViewById(R.id.note_search_card_grid_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        noteGridAdapter = new NoteGridAdapter(this, resultsList);

        recyclerView.setAdapter(noteGridAdapter);
        recyclerView.setHasFixedSize(true);
    }

    private void performSearch(String query) {
        // Filter results based on query
        if (query.length() < 3) {
            Toast.makeText(this, "enter at least 3 characters to search", Toast.LENGTH_SHORT).show();
            return;
        }

        resultsList.clear();
        List<Long> filteredResults = searchInDb(query);

        resultsList.addAll(filteredResults);
        noteGridAdapter.setNoteIds(resultsList);
    }

    private List<Long> searchInDb(String searchTerm) {
        return NoteTextContract.NoteTextQueries.searchTextFromDb(searchTerm, noteTextDbHelper);
    }
}
