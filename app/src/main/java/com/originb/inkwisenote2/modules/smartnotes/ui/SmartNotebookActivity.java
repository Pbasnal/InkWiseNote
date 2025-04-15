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
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import org.greenrobot.eventbus.EventBus;

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
            smartNotebookAdapter.saveNotebookPageAt(
                    viewModel.getCurrentPageIndex().getValue(),
                    viewModel.getSmartNotebook().getValue().getAtomicNotes().get(
                            viewModel.getCurrentPageIndex().getValue()
                    )
            );
            viewModel.navigateToNextPage();
        });
        
        prevButton.setOnClickListener(view -> {
            smartNotebookAdapter.saveNotebookPageAt(
                    viewModel.getCurrentPageIndex().getValue(),
                    viewModel.getSmartNotebook().getValue().getAtomicNotes().get(
                            viewModel.getCurrentPageIndex().getValue()
                    )
            );
            viewModel.navigateToPreviousPage();
        });
        
        newNotePageBtn.setOnClickListener(view -> viewModel.addNewPage());
    }

    private void setupObservers() {
        // Observe smart notebook data changes
        viewModel.getSmartNotebook().observe(this, notebook -> {
            if (smartNotebookAdapter == null) {
                smartNotebookAdapter = new SmartNotebookAdapter(this, notebook);
                recyclerView.setAdapter(smartNotebookAdapter);
            } else {
                smartNotebookAdapter.setSmartNotebook(notebook);
                smartNotebookAdapter.notifyDataSetChanged();
            }
        });
        
        // Observe notebook title
        viewModel.getNotebookTitle().observe(this, title -> {
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
        viewModel.getCurrentPageIndex().observe(this, index -> {
            scrollLayout.setScrollRequested(true);
            recyclerView.smoothScrollToPosition(index);
        });
    }

    @Override
    public void onBackPressed() {
        viewModel.updateTitle(noteTitleText.getText().toString());
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        viewModel.saveCurrentNote(); 
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}

