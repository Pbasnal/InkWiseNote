package com.originb.inkwisenote2.modules.notesearch;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNoteGridAdapter;
import org.koin.android.compat.ViewModelCompat;

import java.util.ArrayList;
import java.util.List;

public class NoteSearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private Button searchButton;
    private SmartNoteGridAdapter smartNoteGridAdapter;
    private NoteSearchViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // 1. Initialize ViewModel with Koin DI
        viewModel = ViewModelCompat.getViewModel(this, NoteSearchViewModel.class);

        // 2. Initialize UI Components
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        setupRecyclerView();

        // 3. Set up Observers (The "Reactive" part)
        observeViewModel();

        // 4. Input events
        searchButton.setOnClickListener(view ->
                viewModel.performSearch(searchInput.getText().toString())
        );
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.note_search_card_grid_view);
        smartNoteGridAdapter = new SmartNoteGridAdapter(this, new ArrayList<>(), false);
        recyclerView.setAdapter(smartNoteGridAdapter);
        recyclerView.setHasFixedSize(true);
    }

    private void observeViewModel() {
        // Update list when search results change
        viewModel.searchResults.observe(this, results -> {
            smartNoteGridAdapter.setSmartNotebooks(results);
        });

        // Show toast when the ViewModel sends a message
        viewModel.toastMessage.observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
