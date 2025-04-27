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
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import com.originb.inkwisenote2.modules.smartnotes.data.NotebookNavigationData;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartNotebookUpdateType;
import com.originb.inkwisenote2.modules.smartnotes.ui.activitystates.ISmartNotebookActivityState;
import com.originb.inkwisenote2.modules.smartnotes.ui.activitystates.IStateManager;
import com.originb.inkwisenote2.modules.smartnotes.viewmodels.SmartNotebookViewModel;
import com.originb.inkwisenote2.modules.smartnotes.viewmodels.SmartNotebookViewModelFactory;

public class SmartNotebookActivity extends AppCompatActivity implements IStateManager {

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
        String bookTitle = getIntent().getStringExtra("bookTitle");

        if (noteIds == null || Strings.isNullOrWhitespace(noteIds)) {
            currentState = new SmartNotebookActivityRWState();
        } else {
            currentState = new SmartNotebookActivityVirtualNotebook(this);
        }

        // Initialize UI components
        initializeRecyclerView();
        currentState.initializeViews();
        currentState.setupObservers();

        viewModel.loadSmartNotebook(bookIdToOpen, workingNotePath, bookTitle, noteIds);
    }

    public void changeState() {
        viewModel.onNotebookIsInDb(isNotebookSaved -> {
            currentState.finalizeState();
            currentState = new SmartNotebookActivityRWState();
            currentState.initializeViews();
        });
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
        newNotePageBtn.setImageResource(R.drawable.ic_add);
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

    public void onSmartNotebookUpdate(SmartNotebookViewModel.SmartNotebookUpdate notebookUpdate) {
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

    }

    public void onNavigationDataChange(NotebookNavigationData navigationData) {
        pageNumText.setText(navigationData.pageNumbeText);
        nextButton.setVisibility(navigationData.showNextButton ? View.VISIBLE : View.INVISIBLE);
        prevButton.setVisibility(navigationData.showPrevButton ? View.VISIBLE : View.INVISIBLE);
    }

    public void onCurrentPageIndexChange(Integer index) {
        recyclerView.post(() -> {
            scrollLayout.setScrollRequested(true);
            recyclerView.smoothScrollToPosition(index);
            smartNotebookAdapter.setNoteData(index, viewModel.getCurrentNote());
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        currentState.saveNotebook();
    }

    @Override
    protected void onStop() {
        super.onStop();
        currentState.saveNotebook();
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

        @Override
        public void finalizeState() {

        }

        @Override
        public void setupObservers() {
            SmartNotebookActivity owner = SmartNotebookActivity.this;
            // Observe smart notebook data changes
            viewModel.getSmartNotebook().observe(owner, notebookUpdate -> {
                onSmartNotebookUpdate(notebookUpdate);
                if (notebookUpdate != null) {
                    // clean up existing observers
                    viewModel.getSmartNotebook().removeObservers(owner);

                    // Set up all the observers
                    // this clean up and setting is done so that, the navigation and current page
                    // listeners do not experience null reference error because
                    // smartNotebook is still null.
                    viewModel.getSmartNotebook().observe(owner, owner::onSmartNotebookUpdate);
                    // Observe page number text
                    viewModel.getNavigationDataLive().observe(owner, owner::onNavigationDataChange);
                    // Observe current page index (for scrolling)
                    viewModel.getCurrentPageIndexLive().observe(owner, owner::onCurrentPageIndexChange);

                }
            });
        }

        @Override
        public void saveNotebook() {
            viewModel.updateTitle(noteTitleText.getText().toString());
            NoteHolderData noteHolderData = smartNotebookAdapter.getNoteData(viewModel.getCurrentNote().getNoteId());
            BackgroundOps.execute(() -> {
                viewModel.saveCurrentNote(noteHolderData);
                viewModel.saveCurrentSmartNotebook();
            });
        }
    }

    public class SmartNotebookActivityVirtualNotebook implements ISmartNotebookActivityState {

        private IStateManager stateManager;

        public SmartNotebookActivityVirtualNotebook(IStateManager stateManager) {
            this.stateManager = stateManager;
        }

        @Override
        public void initializeViews() {
            initializeNavigationButtons();
            initializeSaveButton_VirtualNotebook();
            initializeNoteTitle_VirtualNotebook();
            initializeCreatedTimeAndPageNum();
        }

        @Override
        public void finalizeState() {

        }

        @Override
        public void setupObservers() {
            SmartNotebookActivity owner = SmartNotebookActivity.this;
            // Observe smart notebook data changes
            viewModel.getSmartNotebook().observe(owner, notebookUpdate -> {
                onSmartNotebookUpdate_VirtualNotebook(notebookUpdate);
                if (notebookUpdate != null) {
                    // clean up existing observers
                    viewModel.getSmartNotebook().removeObservers(owner);

                    // Set up all the observers
                    // this clean up and setting is done so that, the navigation and current page
                    // listeners do not experience null reference error because
                    // smartNotebook is still null.
                    viewModel.getSmartNotebook().observe(owner, this::onSmartNotebookUpdate_VirtualNotebook);
                    // Observe page number text
                    viewModel.getNavigationDataLive().observe(owner, owner::onNavigationDataChange);
                    // Observe current page index (for scrolling)
                    viewModel.getCurrentPageIndexLive().observe(owner, owner::onCurrentPageIndexChange);

                }
            });

            // Observe page number text
            viewModel.getNavigationDataLive().observe(owner, owner::onNavigationDataChange);

            // Observe current page index (for scrolling)
            viewModel.getCurrentPageIndexLive().observe(owner, owner::onCurrentPageIndexChange);
        }

        private void onSmartNotebookUpdate_VirtualNotebook(SmartNotebookViewModel.SmartNotebookUpdate smartNotebookUpdate) {
            SmartNotebook notebook = smartNotebookUpdate.smartNotebook;
            if (notebook != null) {
                long bookId = notebook.getSmartBook().getBookId();
                if (bookId != -1) {
                    stateManager.changeState();
                }
            }

            onSmartNotebookUpdate(smartNotebookUpdate);
        }

        @Override
        public void saveNotebook() {
            viewModel.updateTitle(noteTitleText.getText().toString());
            NoteHolderData noteHolderData = smartNotebookAdapter.getNoteData(viewModel.getCurrentNote().getNoteId());
            BackgroundOps.execute(() -> viewModel.saveCurrentNote(noteHolderData));
        }

        private void initializeSaveButton_VirtualNotebook() {
            newNotePageBtn = findViewById(R.id.fab_add_note);
            newNotePageBtn.setImageResource(R.drawable.ic_save);
            newNotePageBtn.setOnClickListener(view -> {
                viewModel.saveCurrentSmartNotebook();
                stateManager.changeState();
            });
        }

        private void initializeNoteTitle_VirtualNotebook() {
            noteTitleText = findViewById(R.id.smart_note_title);
            noteTitleText.setFocusable(false);
            noteTitleText.setCursorVisible(false);
            noteTitleText.setInputType(InputType.TYPE_NULL);
        }
    }


}

