package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.DateTimeUtils;
import com.originb.inkwisenote2.common.Strings;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;

public class SmartNotebookActivity extends AppCompatActivity {

    private Logger logger = new Logger("SmartNotebookActivity");

    private String workingNotePath;

    private SmartNotebookViewModel viewModel;
    private NotePagerAdapter pagerAdapter;
    private ViewPager2 viewPager;

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
        // Initialize ViewPager2 and its adapter
        viewPager = findViewById(R.id.smart_note_page_view);
        pagerAdapter = new NotePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
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
            NoteFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                NoteHolder.NoteHolderData noteData = currentFragment.getNoteHolderData();
                BackgroundOps.execute(() -> viewModel.saveCurrentNote(noteData),
                        viewModel::navigateToNextPage);
            } else {
                viewModel.navigateToNextPage();
            }
        });

        prevButton.setOnClickListener(view -> {
            NoteFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                NoteHolder.NoteHolderData noteData = currentFragment.getNoteHolderData();
                BackgroundOps.execute(() -> viewModel.saveCurrentNote(noteData),
                        viewModel::navigateToPreviousPage);
            } else {
                viewModel.navigateToPreviousPage();
            }
        });

        newNotePageBtn.setOnClickListener(view -> viewModel.addNewPage());
        
        // ViewPager page change listener
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (viewModel.getCurrentPageIndexLive().getValue() != position) {
                    viewModel.getCurrentPageIndexLive().setValue(position);
                }
            }
        });
    }

    private void setupObservers() {
        // Observe smart notebook data changes
        viewModel.getSmartNotebook().observe(this, notebook -> {
            if (notebook != null) {
                pagerAdapter.setSmartNotebook(notebook);
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
            // Only scroll if the current page is different
            if (viewPager.getCurrentItem() != index) {
                viewPager.setCurrentItem(index, true);
            }
        });
        
        // Observe note type changes
        viewModel.getNoteTypeChangedPosition().observe(this, position -> {
            if (position != null && position >= 0) {
                pagerAdapter.notifyNoteTypeChanged(position);
            }
        });
    }
    
    /**
     * Get the current fragment displayed in the ViewPager
     * @return The current NoteFragment, or null if not found
     */
    private NoteFragment getCurrentFragment() {
        // Get the current fragment from the ViewPager
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentByTag("f" + viewPager.getCurrentItem());
        
        if (currentFragment instanceof NoteFragment) {
            return (NoteFragment) currentFragment;
        }
        
        return null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        viewModel.updateTitle(noteTitleText.getText().toString());
        
        NoteFragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            NoteHolder.NoteHolderData noteHolderData = currentFragment.getNoteHolderData();
            BackgroundOps.execute(() -> {
                viewModel.saveCurrentNote(noteHolderData);
                viewModel.saveCurrentSmartNotebook();
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        
        NoteFragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            NoteHolder.NoteHolderData noteHolderData = currentFragment.getNoteHolderData();
            BackgroundOps.execute(() -> {
                viewModel.saveCurrentNote(noteHolderData);
                viewModel.saveCurrentSmartNotebook();
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}

