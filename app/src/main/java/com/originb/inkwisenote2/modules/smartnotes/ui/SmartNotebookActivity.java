package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.os.Bundle;
import android.text.InputType;
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
import com.originb.inkwisenote2.modules.smartnotes.data.SmartNotebookUpdateType;
import com.originb.inkwisenote2.modules.smartnotes.ui.activitystates.ISmartNotebookActivityState;
import com.originb.inkwisenote2.modules.smartnotes.viewmodels.SmartNotebookViewModel;
import com.originb.inkwisenote2.modules.smartnotes.viewmodels.SmartNotebookViewModelFactory;

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

    private ISmartNotebookActivityState currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_note);

        // Initialize ViewModel using the factory
        SmartNotebookViewModelFactory factory = new SmartNotebookViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(SmartNotebookViewModel.class);

        // Load notebook data
        Long bookIdToOpen = getIntent().getLongExtra("bookId", -1);
        workingNotePath = getIntent().getStringExtra("workingNotePath");
        String noteIds = getIntent().getStringExtra("noteIds");

        if (noteIds == null || Strings.isNullOrWhitespace(noteIds)) {
            currentState = new SmartNotebookActivityRWState();
        } else {
            currentState = new SmartNotebookActivityReadOnlyState();
        }

        // Initialize UI components
        initializeRecyclerView();
        currentState.initializeViews();
        setupObservers();

        viewModel.loadSmartNotebook(bookIdToOpen, workingNotePath, noteIds);
    }

    private void initializeRecyclerView() {
        // Initialize RecyclerView and its components
        recyclerView = findViewById(R.id.smart_note_page_view);
        scrollLayout = new SmartNotebookPageScrollLayout(this);
        recyclerView.addOnScrollListener(new SmartNotebookScrollListener(scrollLayout));
        recyclerView.setLayoutManager(scrollLayout);
    }

    public void initializeNavigationButtons() {
        // Initialize buttons
        nextButton = findViewById(R.id.fab_next_note);
        prevButton = findViewById(R.id.fab_prev_note);

        // Initially hide navigation buttons
        nextButton.setVisibility(View.INVISIBLE);
        prevButton.setVisibility(View.INVISIBLE);

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
    }

    public void initializeNewNoteButton() {
        newNotePageBtn = findViewById(R.id.fab_add_note);
        newNotePageBtn.setVisibility(View.VISIBLE);
        newNotePageBtn.setOnClickListener(view -> {
            AtomicNoteEntity atomicNote = viewModel.getCurrentNote();
            NoteHolderData noteData = smartNotebookAdapter.getNoteData(atomicNote.getNoteId());
            BackgroundOps.execute(() -> viewModel.saveCurrentNote(noteData),
                    viewModel::addNewPage);
        });
    }

    public void initializeNoteTitle() {
        noteTitleText = findViewById(R.id.smart_note_title);

        noteTitleText.setOnClickListener((view) -> noteTitleText.selectAll());
        noteTitleText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) noteTitleText.selectAll();
        });
    }

    public void initializeCreatedTimeAndPageNum() {
        // Initialize text fields
        noteCreatedTime = findViewById(R.id.note_created_time);
        pageNumText = findViewById(R.id.page_num_text);
    }

    private void setupObservers() {
        // Observe smart notebook data changes
        viewModel.getSmartNotebook().observe(this, notebookUpdate -> {
            if (notebookUpdate.notbookUpdateType == SmartNotebookUpdateType.NOTEBOOK_DELETED) {
                finish();
            }
            if (smartNotebookAdapter == null) {
                smartNotebookAdapter = new SmartNotebookAdapter(this, notebookUpdate.smartNotebook);
                recyclerView.setAdapter(smartNotebookAdapter);
            }

            if (notebookUpdate.notbookUpdateType == SmartNotebookUpdateType.NOTE_DELETED) {
                smartNotebookAdapter.removeNoteCard(notebookUpdate.atomicNote.getNoteId());
            } else if (notebookUpdate.indexOfUpdatedNote == -1) {
                smartNotebookAdapter.setSmartNotebook(notebookUpdate.smartNotebook);
            } else {
                smartNotebookAdapter.setSmartNotebook(notebookUpdate.smartNotebook, notebookUpdate.indexOfUpdatedNote);
            }
            String createdTime = DateTimeUtils.msToDateTime(notebookUpdate.smartNotebook.smartBook.getLastModifiedTimeMillis());
            noteCreatedTime.setText(createdTime);
            noteTitleText.setText(notebookUpdate.smartNotebook.smartBook.getTitle());
        });

        // Observe page number text
        viewModel.getNavigationDataLive().observe(this, navigationData -> {
            pageNumText.setText(navigationData.pageNumbeText);
            nextButton.setVisibility(navigationData.showNextButton ? View.VISIBLE : View.INVISIBLE);
            prevButton.setVisibility(navigationData.showPrevButton ? View.VISIBLE : View.INVISIBLE);
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

    public class SmartNotebookActivityRWState implements ISmartNotebookActivityState {

        @Override
        public void initializeViews() {
            initializeNavigationButtons();
            initializeNewNoteButton();
            initializeNoteTitle();
            initializeCreatedTimeAndPageNum();
        }
    }

    public class SmartNotebookActivityReadOnlyState implements ISmartNotebookActivityState {

        @Override
        public void initializeViews() {
            initializeNavigationButtons();
            initializeNoteTitle_ReadOnly();
            initializeCreatedTimeAndPageNum();
        }

        public void initializeNoteTitle_ReadOnly() {
            noteTitleText = findViewById(R.id.smart_note_title);
            noteTitleText.setFocusable(false);
            noteTitleText.setCursorVisible(false);
            noteTitleText.setInputType(InputType.TYPE_NULL);
        }

        // Later this should be the save button which will turn this readonly notebook to read-write
        public void initializeNewNoteButton_ReadOnly() {
            newNotePageBtn = findViewById(R.id.fab_add_note);
            newNotePageBtn.setEnabled(false);
        }
    }
}

