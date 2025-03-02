package com.originb.inkwisenote.ux.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.data.dao.noteocr.NoteOcrTextDao;
import com.originb.inkwisenote.data.entities.noteocrdata.NoteOcrText;
import com.originb.inkwisenote.modules.repositories.Repositories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NoteSearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private Button searchButton;
    private Set<Long> resultsList;

    private NoteOcrTextDao noteOcrTextDao;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();

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
        resultsList = new HashSet<>();

        recyclerView = findViewById(R.id.note_search_card_grid_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);



//        recyclerView.setAdapter(noteGridAdapter);
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
//        noteGridAdapter.setNoteIds(resultsList);
    }

    private List<Long> searchInDb(String searchTerm) {
        return noteOcrTextDao.searchTextFromDb(searchTerm).stream()
                .map(NoteOcrText::getNoteId)
                .collect(Collectors.toList());
    }
}
