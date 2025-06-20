package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.DateTimeUtils;
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.common.Strings;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.*;
import com.originb.inkwisenote2.modules.smartnotes.ui.activitystates.ISmartNotebookActivityState;
import com.originb.inkwisenote2.modules.smartnotes.ui.activitystates.IStateManager;
import com.originb.inkwisenote2.modules.smartnotes.viewmodels.SmartNotebookViewModel;
import com.originb.inkwisenote2.modules.smartnotes.viewmodels.SmartNotebookViewModelFactory;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;

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
    private ImageButton backButton;

    private Long noteIdToLoadOnOpen;
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
        noteIdToLoadOnOpen = getIntent().getLongExtra("selectedNoteId", -1);

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
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scrollLayout = new SmartNotebookPageScrollLayout(this);
        recyclerView.addOnScrollListener(new SmartNotebookScrollListener(scrollLayout));
        recyclerView.setLayoutManager(scrollLayout);

        recyclerView.setOnFlingListener(null);
        recyclerView.setNestedScrollingEnabled(false);
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
            if (atomicNote == null) return;

            NoteHolderData noteData = smartNotebookAdapter.getNoteData(atomicNote.getNoteId());
            if (noteData == null) return;

            BackgroundOps.execute(() -> viewModel.saveCurrentNote(viewModel.getCurrentNote(), noteData),
                    viewModel::navigateToNextPage);
        });

        prevButton.setOnClickListener(view -> {
            AtomicNoteEntity atomicNote = viewModel.getCurrentNote();
            if (atomicNote == null) return;

            NoteHolderData noteData = smartNotebookAdapter.getNoteData(atomicNote.getNoteId());
            if (noteData == null) return;

            BackgroundOps.execute(() -> viewModel.saveCurrentNote(viewModel.getCurrentNote(), noteData),
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
            BackgroundOps.execute(() -> viewModel.saveCurrentNote(viewModel.getCurrentNote(), noteData),
                    viewModel::addNewPage);
        });
    }

    public void initializeNoteTitle() {
        noteTitleText = findViewById(R.id.smart_note_title);
        noteTitleText.setOnClickListener((view) -> noteTitleText.selectAll());

        noteTitleText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) noteTitleText.selectAll();
        });

        noteTitleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    noteTitleText.setAlpha(1.0f);
                } else {
                    noteTitleText.setAlpha(0.7f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void initializeCreatedTimeAndPageNum() {
        // Initialize text fields
        noteCreatedTime = findViewById(R.id.note_created_time);
        pageNumText = findViewById(R.id.page_num_text);
    }

    public void initializeBackButton() {
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> {
            // Save current state before going back
            AtomicNoteEntity atomicNote = viewModel.getCurrentNote();
            if (atomicNote != null) {
                NoteHolderData noteData = smartNotebookAdapter.getNoteData(atomicNote.getNoteId());
                if (noteData != null) {
                    BackgroundOps.execute(() -> viewModel.saveCurrentNote(atomicNote, noteData),
                            () -> finish());
                } else {
                    finish();
                }
            } else {
                finish();
            }
        });
    }

    /**
     * Hide the navigation bar for immersive drawing experience
     */
    public void hideNavigationBar() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.navigationBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    /**
     * Show the navigation bar
     */
    public void showNavigationBar() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.show(WindowInsetsCompat.Type.navigationBars());
    }

    /**
     * Handle system UI visibility changes to maintain immersive mode
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Check if current fragment is HandwrittenNoteFragment and hide navigation bar if needed
            if (smartNotebookAdapter != null) {
                AtomicNoteEntity currentNote = viewModel.getCurrentNote();
                if (currentNote != null && "handwritten_png".equals(currentNote.getNoteType())) {
                    hideNavigationBar();
                }
            }
        }
    }

    public void onSmartNotebookUpdate(SmartNotebookViewModel.SmartNotebookUpdate notebookUpdate) {
        if (isFinishing() || isDestroyed()) {
            return; // Don't proceed if activity is finishing/destroyed
        }

        if (notebookUpdate.notbookUpdateType == SmartNotebookUpdateType.NOTEBOOK_DELETED) {
            currentState = new SmartNotebookDeletedNotebook();
            Routing.HomePageActivity.openSmartHomePageAndStartFresh(this);
            return;
        }

        if (smartNotebookAdapter == null) {
            smartNotebookAdapter = new SmartNotebookAdapter(this, notebookUpdate.smartNotebook);
            if (recyclerView != null) {
                recyclerView.setAdapter(smartNotebookAdapter);
            }
        }

        if (notebookUpdate.notbookUpdateType == SmartNotebookUpdateType.NOTE_DELETED) {
            if (smartNotebookAdapter != null) {
                smartNotebookAdapter.removeNoteCard(notebookUpdate.atomicNote.getNoteId());
            }
        } else if (notebookUpdate.indexOfUpdatedNote == -1) {
            if (smartNotebookAdapter != null) {
                smartNotebookAdapter.setSmartNotebook(notebookUpdate.smartNotebook);
            }
        } else {
            if (smartNotebookAdapter != null) {
                smartNotebookAdapter.setSmartNotebook(notebookUpdate.smartNotebook, notebookUpdate.indexOfUpdatedNote);
            }
        }

        // Update UI elements if available
        String createdTime = DateTimeUtils.msToDateTime(notebookUpdate.smartNotebook.smartBook.getLastModifiedTimeMillis());
        if (noteCreatedTime != null) {
            noteCreatedTime.setText(createdTime);
        }

        if (noteTitleText != null) {
            String noteTitle = noteTitleText.getText().toString().trim();
            if (Strings.isNullOrWhitespace(noteTitle)) {
                String smartBookName = notebookUpdate.smartNotebook.smartBook.getTitle();
                if (Strings.isNotEmpty(smartBookName)) {
                    noteTitleText.setText(notebookUpdate.smartNotebook.smartBook.getTitle());
                    noteTitleText.setAlpha(1.0f);
                }
            }
        }

        if (noteIdToLoadOnOpen != null && noteIdToLoadOnOpen != -1L) {
            List<AtomicNoteEntity> allNotes = notebookUpdate.smartNotebook.atomicNotes;
            int i = 0;
            for (; i < allNotes.size(); i++) {
                if (noteIdToLoadOnOpen.equals(allNotes.get(i).getNoteId())) {
                    break;
                }
            }
            viewModel.navigateToPageIndex(i);
            noteIdToLoadOnOpen = -1L;
        }
    }

    public void onNavigationDataChange(NotebookNavigationData navigationData) {
        if (isFinishing() || isDestroyed() || navigationData == null) {
            return;
        }

        if (pageNumText != null) {
            pageNumText.setText(navigationData.pageNumbeText);
        }

        if (nextButton != null) {
            nextButton.setVisibility(navigationData.showNextButton ? View.VISIBLE : View.INVISIBLE);
        }

        if (prevButton != null) {
            prevButton.setVisibility(navigationData.showPrevButton ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void onCurrentPageIndexChange(Integer index) {
        if (recyclerView == null || scrollLayout == null || smartNotebookAdapter == null) {
            return; // Guard against null references
        }

        recyclerView.postDelayed(() -> {
            if (isDestroyed() || isFinishing()) {
                return; // Don't proceed if activity is finishing/destroyed
            }

            scrollLayout.setScrollRequested(true);
            recyclerView.smoothScrollToPosition(index);

            // Get current note and ensure it's not null before trying to set data
            AtomicNoteEntity currentNote = viewModel.getCurrentNote();
            if (currentNote != null) {
                smartNotebookAdapter.setNoteData(index, currentNote);
            }
        }, 100);
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

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Recreate all visual elements to apply new theme
        recreateVisuals();

        // After view recreation is complete, recreate all fragments with new theme
        if (recyclerView != null) {
            recyclerView.post(this::recreateFragments);
        }
    }

    private void recreateVisuals() {
        // Get current content view to be replaced
        ViewGroup rootView = findViewById(android.R.id.content);
        if (rootView == null) return;

        // Save state of important elements
        AtomicNoteEntity currentNote = null;
        if (viewModel != null) {
            currentNote = viewModel.getCurrentNote();
        }
        Integer currentIndex = null;
        if (viewModel != null && viewModel.getCurrentPageIndexLive() != null) {
            currentIndex = viewModel.getCurrentPageIndexLive().getValue();
        }
        SmartNotebook currentNotebook = null;
        if (viewModel != null && viewModel.getSmartNotebookUpdate().getValue() != null) {
            currentNotebook = viewModel.getSmartNotebookUpdate().getValue().smartNotebook;
        }

        // Re-apply theme by recreating the views
        setContentView(R.layout.activity_smart_note);

        // Reinitialize the UI elements
        initializeRecyclerView();

        // Create new adapter with the notebook data
        if (currentNotebook != null) {
            smartNotebookAdapter = new SmartNotebookAdapter(this, currentNotebook);
            recyclerView.setAdapter(smartNotebookAdapter);
        }

        // Re-initialize the current state
        if (currentState != null) {
            currentState.initializeViews();
            currentState.setupObservers();
        }

        // Update the adapter with the current notebook data
        if (viewModel != null && viewModel.getSmartNotebookUpdate().getValue() != null) {
            SmartNotebookViewModel.SmartNotebookUpdate update = viewModel.getSmartNotebookUpdate().getValue();
            onSmartNotebookUpdate(update);

            // If we had a note and index already, restore the position
            if (currentIndex != null) {
                onCurrentPageIndexChange(currentIndex);
            }
        }
    }

    private void recreateFragments() {
        if (smartNotebookAdapter == null || viewModel == null) return;

        // Force recreate all fragments to apply new theme
        SmartNotebookViewModel.SmartNotebookUpdate update = viewModel.getSmartNotebookUpdate().getValue();
        if (update != null) {
            // Set notebook to null and back to force a full refresh of all fragments
            smartNotebookAdapter.setSmartNotebook(null);
            smartNotebookAdapter.setSmartNotebook(update.smartNotebook);

            // Ensure we're showing the current page
            Integer currentPageIndex = viewModel.getCurrentPageIndexLive().getValue();
            if (currentPageIndex != null) {
                onCurrentPageIndexChange(currentPageIndex);
            }
        }
    }

    public class SmartNotebookActivityRWState implements ISmartNotebookActivityState {

        @Override
        public void initializeViews() {
            initializeNavigationButtons();
            initializeNewNoteButton();
            initializeNoteTitle();
            initializeCreatedTimeAndPageNum();
            initializeBackButton();
        }

        @Override
        public void finalizeState() {
            // Remove observers to prevent duplicates
            if (viewModel != null) {
                viewModel.getSmartNotebookUpdate().removeObservers(SmartNotebookActivity.this);
                viewModel.getNavigationDataLive().removeObservers(SmartNotebookActivity.this);
                viewModel.getCurrentPageIndexLive().removeObservers(SmartNotebookActivity.this);
            }
        }

        @Override
        public void setupObservers() {
            SmartNotebookActivity owner = SmartNotebookActivity.this;

            // First remove any existing observers to prevent duplication during configuration changes
            if (viewModel != null) {
                viewModel.getSmartNotebookUpdate().removeObservers(owner);
                viewModel.getNavigationDataLive().removeObservers(owner);
                viewModel.getCurrentPageIndexLive().removeObservers(owner);
            }

            // Observe smart notebook data changes
            viewModel.getSmartNotebookUpdate().observe(owner, notebookUpdate -> {
                if (notebookUpdate != null) {
                    onSmartNotebookUpdate(notebookUpdate);
                }
            });

            // Observe page number text
            viewModel.getNavigationDataLive().observe(owner, navigationData -> {
                if (navigationData != null) {
                    onNavigationDataChange(navigationData);
                }
            });

            // Observe current page index (for scrolling)
            viewModel.getCurrentPageIndexLive().observe(owner, index -> {
                if (index != null) {
                    onCurrentPageIndexChange(index);
                }
            });
        }

        @Override
        public void saveNotebook() {
            String oldNotebookTitle = viewModel.getNotebookTitle().getValue();
            String updatedTitle = noteTitleText.getText().toString().trim();
            String createTimeMillis = String.valueOf(viewModel.getCreatedTimeMillis().getValue());
            final String notebookTitle = Strings.isNotEmpty(updatedTitle) ? updatedTitle : createTimeMillis;
            boolean notebookNameNotChanged = oldNotebookTitle != null && oldNotebookTitle.equals(notebookTitle);

            boolean titleUpdated = !notebookNameNotChanged && viewModel.updateTitle(updatedTitle);

            if (!titleUpdated) {
                NoteHolderData noteHolderData = smartNotebookAdapter.getNoteData(viewModel.getCurrentNote().getNoteId());
                BackgroundOps.execute(() -> {
                    viewModel.saveCurrentNote(viewModel.getCurrentNote(), noteHolderData);
                    viewModel.saveCurrentSmartNotebook();
                });
            } else {
                viewModel.saveCurrentSmartNotebook();
                String newNotebookPath = workingNotePath + "/" + notebookTitle;
                boolean isRenamed = viewModel.renameNotebookFolderName(newNotebookPath, oldNotebookTitle);
                SmartNotebook notebook = Objects.requireNonNull(viewModel.getSmartNotebookUpdate().getValue()).smartNotebook;
                boolean notesHaveNewPath = true;

                for (AtomicNoteEntity atomicNote : notebook.atomicNotes) {
                    String noteFilePath = atomicNote.getFilepath();
                    notesHaveNewPath &= newNotebookPath.equals(noteFilePath);
                }

                if (isRenamed || !notesHaveNewPath) {
                    logger.debug("Folder renamed successfully.");
                    for (AtomicNoteEntity atomicNote : notebook.atomicNotes) {
                        NoteHolderData noteHolderData = smartNotebookAdapter.getNoteData(atomicNote.getNoteId());
                        viewModel.saveNoteInCorrectFolder(atomicNote, newNotebookPath, noteHolderData);
                    }
                } else {
                    logger.debug("Failed to rename folder.");
                }
            }
        }
    }

    public class SmartNotebookDeletedNotebook implements ISmartNotebookActivityState {
        @Override
        public void initializeViews() {

        }

        @Override
        public void finalizeState() {

        }

        @Override
        public void setupObservers() {

        }

        @Override
        public void saveNotebook() {

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
            initializeBackButton();
        }

        @Override
        public void finalizeState() {
            // Remove observers to prevent duplicates
            if (viewModel != null) {
                viewModel.getSmartNotebookUpdate().removeObservers(SmartNotebookActivity.this);
                viewModel.getNavigationDataLive().removeObservers(SmartNotebookActivity.this);
                viewModel.getCurrentPageIndexLive().removeObservers(SmartNotebookActivity.this);
            }
        }

        @Override
        public void setupObservers() {
            SmartNotebookActivity owner = SmartNotebookActivity.this;

            // First remove any existing observers to prevent duplication during configuration changes
            if (viewModel != null) {
                viewModel.getSmartNotebookUpdate().removeObservers(owner);
                viewModel.getNavigationDataLive().removeObservers(owner);
                viewModel.getCurrentPageIndexLive().removeObservers(owner);
            }

            // Observe smart notebook data changes
            viewModel.getSmartNotebookUpdate().observe(owner, smartNotebookUpdate -> {
                if (smartNotebookUpdate != null) {
                    onSmartNotebookUpdate_VirtualNotebook(smartNotebookUpdate);
                    onSmartNotebookUpdate(smartNotebookUpdate);
                }
            });

            // Observe page number text
            viewModel.getNavigationDataLive().observe(owner, navigationData -> {
                if (navigationData != null) {
                    onNavigationDataChange(navigationData);
                }
            });

            // Observe current page index (for scrolling)
            viewModel.getCurrentPageIndexLive().observe(owner, index -> {
                if (index != null) {
                    onCurrentPageIndexChange(index);
                }
            });
        }

        private void onSmartNotebookUpdate_VirtualNotebook(SmartNotebookViewModel.SmartNotebookUpdate smartNotebookUpdate) {
            SmartNotebook notebook = smartNotebookUpdate.smartNotebook;
            if (notebook != null) {
                long bookId = notebook.getSmartBook().getBookId();
                if (bookId != -1) {
                    stateManager.changeState();
                }
            }
        }

        @Override
        public void saveNotebook() {
            AtomicNoteEntity atomicNote = viewModel.getCurrentNote();
            NoteHolderData noteHolderData = smartNotebookAdapter.getNoteData(atomicNote.getNoteId());
            BackgroundOps.execute(() -> viewModel.saveCurrentNote(atomicNote, noteHolderData));
            // SmartNotebook is not saved
        }

        private void initializeSaveButton_VirtualNotebook() {
            newNotePageBtn = findViewById(R.id.fab_add_note);
            newNotePageBtn.setVisibility(View.GONE);
        }

        private void initializeNoteTitle_VirtualNotebook() {
            noteTitleText = findViewById(R.id.smart_note_title);
            noteTitleText.setFocusable(false);
            noteTitleText.setCursorVisible(false);
            noteTitleText.setInputType(InputType.TYPE_NULL);
        }
    }
}

