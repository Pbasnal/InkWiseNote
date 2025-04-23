package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.DateTimeUtils;
import com.originb.inkwisenote2.common.Strings;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;

public class SmartNotebookActivity extends AppCompatActivity {

    private Logger logger = new Logger("SmartNotebookActivity");

    private String workingNotePath;

    private SmartNotebookViewModel viewModel;
    private SmartNotebookPageScrollLayout scrollLayout;
    private SmartNotebookAdapter smartNotebookAdapter;
    private RecyclerView recyclerView;

    private FloatingActionButton nextButton;
    private FloatingActionButton prevButton;
    private FloatingActionButton newNotePageBtn;
    private EditText noteTitleText;
    private TextView noteCreatedTime;
    private TextView pageNumText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_note);

        // Initialize ViewModel using the factory
        SmartNotebookViewModelFactory factory = new SmartNotebookViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(SmartNotebookViewModel.class);

        // Initialize UI components
        initializeViews();
        setupListeners();
        setupObservers();

        // Load notebook data
        Long bookIdToOpen = getIntent().getLongExtra("bookId", -1);
        workingNotePath = getIntent().getStringExtra("workingNotePath");
        String noteIds = getIntent().getStringExtra("noteIds");
        viewModel.loadSmartNotebook(bookIdToOpen, workingNotePath, noteIds);
    }

    private void initializeViews() {
        // Initialize RecyclerView and its components
        recyclerView = findViewById(R.id.smart_note_page_view);
        scrollLayout = new SmartNotebookPageScrollLayout(this);
        recyclerView.addOnScrollListener(new SmartNotebookScrollListener(scrollLayout));
        recyclerView.setLayoutManager(scrollLayout);

        // Initialize buttons
        nextButton = findViewById(R.id.fab_next_note);
        prevButton = findViewById(R.id.fab_prev_note);
        newNotePageBtn = findViewById(R.id.fab_add_note);

        // Initialize text fields
        noteTitleText = findViewById(R.id.smart_note_title);
        noteCreatedTime = findViewById(R.id.note_created_time);
        pageNumText = findViewById(R.id.page_num_text);

        // Initially hide navigation buttons
        nextButton.setVisibility(View.INVISIBLE);
        prevButton.setVisibility(View.INVISIBLE);
    }

    private void setupListeners() {
        // Note title text listeners
        noteTitleText.setOnClickListener((view) -> noteTitleText.selectAll());
        noteTitleText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) noteTitleText.selectAll();
        });

        // Button click listeners
        nextButton.setOnClickListener(view -> {
            AtomicNoteEntity atomicNote = viewModel.getCurrentNote();
            NoteHolderData noteData = smartNotebookAdapter.getNoteData(atomicNote.getNoteId());

            BackgroundOps.execute(() -> viewModel.saveCurrentNote(noteData),
                    viewModel::navigateToNextPage);
        });

        prevButton.setOnClickListener(view -> {
            AtomicNoteEntity atomicNote = viewModel.getCurrentNote();
            NoteHolderData noteData = smartNotebookAdapter.getNoteData(atomicNote.getNoteId());
            BackgroundOps.execute(() -> viewModel.saveCurrentNote(noteData),
                    viewModel::navigateToPreviousPage);
        });

        newNotePageBtn.setOnClickListener(view -> {
            AtomicNoteEntity atomicNote = viewModel.getCurrentNote();
            NoteHolderData noteData = smartNotebookAdapter.getNoteData(atomicNote.getNoteId());
            BackgroundOps.execute(() -> viewModel.saveCurrentNote(noteData),
                    viewModel::addNewPage);
        });
    }

    private void setupObservers() {
        // Observe smart notebook data changes
        viewModel.getSmartNotebook().observe(this, notebookUpdate -> {
            if (notebookUpdate == null) {
                finish();
            }
            if (smartNotebookAdapter == null) {
                smartNotebookAdapter = new SmartNotebookAdapter(this, notebookUpdate.smartNotebook);
                recyclerView.setAdapter(smartNotebookAdapter);
            }

            if (notebookUpdate.notbookUpdateType == SmartNotebookViewModel.SmartNotebookUpdate.NOTE_DELETED) {
                smartNotebookAdapter.removeNoteCard(notebookUpdate.atomicNote.getNoteId());
            } else if (notebookUpdate.indexOfUpdatedNote == -1) {
                smartNotebookAdapter.setSmartNotebook(notebookUpdate.smartNotebook);
            } else {
                smartNotebookAdapter.setSmartNotebook(notebookUpdate.smartNotebook, notebookUpdate.indexOfUpdatedNote);
            }
        });

        // Observe notebook title
        viewModel.getNotebookTitle().observe(this, title -> {
            if (Strings.isNullOrWhitespace(title)) return;
            if (!title.equals(noteTitleText.getText().toString())) {
                noteTitleText.setText(title);
            }
        });

        // Observe created time
        viewModel.getCreatedTimeMillis().observe(this, createdTime -> {
            noteCreatedTime.setText(DateTimeUtils.msToDateTime(createdTime));
        });

        // Observe page number text
        viewModel.getPageNumberText().observe(this, pageNum -> {
            pageNumText.setText(pageNum);
        });

        // Observe navigation button visibility
        viewModel.getShowNextButton().observe(this, show -> {
            nextButton.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        });

        viewModel.getShowPrevButton().observe(this, show -> {
            prevButton.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        });

        // Observe current page index (for scrolling)
        viewModel.getCurrentPageIndexLive().observe(this, index -> {
            recyclerView.post(() -> {
                scrollLayout.setScrollRequested(true);
                recyclerView.smoothScrollToPosition(index);
                smartNotebookAdapter.setNoteData(index, viewModel.getCurrentNote());
            });
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        viewModel.updateTitle(noteTitleText.getText().toString());
        NoteHolderData noteHolderData = smartNotebookAdapter.getNoteData(viewModel.getCurrentNote().getNoteId());
        BackgroundOps.execute(() -> {
            viewModel.saveCurrentNote(noteHolderData);
            viewModel.saveCurrentSmartNotebook();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        NoteHolderData noteHolderData = smartNotebookAdapter.getNoteData(viewModel.getCurrentNote().getNoteId());
        BackgroundOps.execute(() -> {
            viewModel.saveCurrentNote(noteHolderData);
            viewModel.saveCurrentSmartNotebook();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}

