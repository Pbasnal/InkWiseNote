package com.originb.inkwisenote2.modules.noterelation.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.modules.noterelation.data.RelatedNotesUiState;
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNoteGridAdapter;
import com.originb.inkwisenote2.common.DateTimeUtils;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity;
import com.originb.inkwisenote2.common.Routing;
import org.koin.android.compat.ViewModelCompat;

import java.util.ArrayList;
import java.util.Optional;

public class RelatedNotesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SmartNoteGridAdapter smartNoteGridAdapter;
    private RelatedNotesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_related_notes);

        // Koin Injection
        viewModel = ViewModelCompat.getViewModel(this, RelatedNotesViewModel.class);

        setupRecyclerView();
        observeViewModel();

        long rootBookId = getIntent().getLongExtra("book_id", 0);
        viewModel.loadRelatedNotes(rootBookId);
    }

    private void observeViewModel() {
        viewModel.uiState.observe(this, state -> {
            renderRootNote(state);
            smartNoteGridAdapter.updateNoteRelations(state.relations);
            smartNoteGridAdapter.setSmartNotebooks(state.relatedBooks);
        });

        viewModel.noteDeletedEvent.observe(this, deleted -> {
            if (deleted) Routing.HomePageActivity.openSmartHomePageAndStartFresh(this);
        });
    }

    private void renderRootNote(RelatedNotesUiState state) {
        View includedCard = findViewById(R.id.main_note_card);
        ImageView cardImage = includedCard.findViewById(R.id.card_image);
        TextView cardTitle = includedCard.findViewById(R.id.card_name);
        ImageButton deleteButton = includedCard.findViewById(R.id.btn_dlt_note);

        state.rootImage.noteImage.ifPresent(cardImage::setImageBitmap);
        SmartBookEntity smartBook = state.rootNotebook.getSmartBook();

        String noteTitle = Optional.ofNullable(smartBook.getTitle())
                .filter(title -> !title.trim().isEmpty())
                .orElse(DateTimeUtils.msToDateTime(smartBook.getLastModifiedTimeMillis()));

        cardTitle.setText(noteTitle);

        cardImage.setOnClickListener(v -> Routing.SmartNotebookActivity
                .openNotebookIntent(this, getFilesDir().getPath(), smartBook.getBookId()));

        deleteButton.setOnClickListener(v -> viewModel.deleteRootNote(state.rootNotebook));
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.related_note_card_grid_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        smartNoteGridAdapter = new SmartNoteGridAdapter(this, new ArrayList<>(), false);
        recyclerView.setAdapter(smartNoteGridAdapter);
    }
}