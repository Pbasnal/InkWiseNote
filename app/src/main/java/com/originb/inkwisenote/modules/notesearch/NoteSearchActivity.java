package com.originb.inkwisenote.modules.notesearch;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote.modules.ocr.data.NoteOcrText;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote.modules.smartnotes.ui.SmartNoteGridAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NoteSearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private Button searchButton;
    private List<SmartNotebook> resultsList;

    private SmartNoteGridAdapter smartNoteGridAdapter;

    private NoteOcrTextDao noteOcrTextDao;
    private SmartNotebookRepository smartNotebookRepository;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();

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
        smartNoteGridAdapter = new SmartNoteGridAdapter(this, new ArrayList<>());

        recyclerView.setAdapter(smartNoteGridAdapter);
        recyclerView.setHasFixedSize(true);
    }

    private void performSearch(String query) {
        // Filter results based on query
        if (query.length() < 3) {
            Toast.makeText(this, "enter at least 3 characters to search", Toast.LENGTH_SHORT).show();
            return;
        }

        resultsList.clear();
        BackgroundOps.execute(() -> {
                    Set<SmartNotebook> smartNotebooks = smartNotebookRepository.getSmartNotebooks(query);
                    List<NoteOcrText> noteOcrs = noteOcrTextDao.searchTextFromDb(query);
                    if(noteOcrs != null && !noteOcrs.isEmpty()) {
                        Set<Long> noteIds = noteOcrs.stream()
                                .map(NoteOcrText::getNoteId)
                                .collect(Collectors.toSet());
                        smartNotebooks.addAll(smartNotebookRepository.getSmartNotebooksForNoteIds(noteIds));

                    }
                    return smartNotebooks;
                },
                smartNotebooks -> {
                    resultsList.addAll(smartNotebooks);
                    smartNoteGridAdapter.setSmartNotebooks(resultsList);
                });
    }
}
