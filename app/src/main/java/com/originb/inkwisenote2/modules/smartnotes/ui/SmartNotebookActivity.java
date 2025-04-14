package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.DateTimeUtils;
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SmartNotebookActivity extends AppCompatActivity implements SmartNotebookAdapter.PageSaveListener {

    private SmartNotebookViewModel viewModel;
    private String workingNotePath;
    
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

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SmartNotebookViewModel.class);
        
        createNoteTitleEditText();
        createNoteCreatedTimeText();
        pageNumText = findViewById(R.id.page_num_text);

        // Get intent data
        workingNotePath = getIntent().getStringExtra("workingNotePath");
        Long bookId = getIntent().hasExtra("bookId") ? getIntent().getLongExtra("bookId", -1) : null;
        String noteIds = getIntent().hasExtra("noteIds") ? getIntent().getStringExtra("noteIds") : null;
        
        // Load the notebook
        viewModel.loadSmartNotebook(bookId, workingNotePath, noteIds);

        // Setup RecyclerView and adapter
        recyclerView = findViewById(R.id.smart_note_page_view);
        scrollLayout = new SmartNotebookPageScrollLayout(this);
        recyclerView.addOnScrollListener(new SmartNotebookScrollListener(scrollLayout));
        recyclerView.setLayoutManager(scrollLayout);

        smartNotebookAdapter = new SmartNotebookAdapter(this, null, this);
        recyclerView.setAdapter(smartNotebookAdapter);

        createNextNoteButton();
        createPrevNoteButton();
        createNewNoteButton();
        
        // Observe ViewModel data
        viewModel.getSmartNotebook().observe(this, smartNotebook -> {
            smartNotebookAdapter.setSmartNotebook(smartNotebook);
            noteTitleText.setText(smartNotebook.smartBook.getTitle());
            noteCreatedTime.setText(DateTimeUtils.msToDateTime(smartNotebook.smartBook.getCreatedTimeMillis()));
        });
        
        viewModel.getCurrentPageIndex().observe(this, index -> {
            if (index != null) {
                scrollLayout.setScrollRequested(true);
                recyclerView.smoothScrollToPosition(index);
                updatePageNumberText();
            }
        });
        
        viewModel.getShowNextButton().observe(this, show -> 
            nextButton.setVisibility(show ? View.VISIBLE : View.INVISIBLE));
        
        viewModel.getShowPrevButton().observe(this, show -> 
            prevButton.setVisibility(show ? View.VISIBLE : View.INVISIBLE));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteDelete(Events.NoteDeleted noteDeleted) {
        viewModel.deleteNote(noteDeleted.atomicNote);
    }
    
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotebookDeleted(Events.NotebookDeleted event) {
        Routing.HomePageActivity.openHomePageAndStartFresh(this);
    }

    private void updatePageNumberText() {
        Integer currentPage = viewModel.getCurrentPageIndex().getValue();
        Integer total = viewModel.getTotalPages().getValue();
        
        if (currentPage != null && total != null) {
            pageNumText.setText(String.format("%d/%d", currentPage + 1, total));
        }
    }

    private void createNoteTitleEditText() {
        noteTitleText = this.findViewById(R.id.smart_note_title);
        noteTitleText.setOnClickListener((view) ->
                noteTitleText.selectAll()
        );
        noteTitleText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) noteTitleText.selectAll();
        });
    }

    private void createNoteCreatedTimeText() {
        noteCreatedTime = this.findViewById(R.id.note_created_time);
    }

    private void createNewNoteButton() {
        newNotePageBtn = findViewById(R.id.fab_add_note);
        newNotePageBtn.setOnClickListener(v -> viewModel.addNewPage());
    }

    private void createNextNoteButton() {
        nextButton = findViewById(R.id.fab_next_note);
        nextButton.setOnClickListener(view -> {
            Integer currentIndex = viewModel.getCurrentPageIndex().getValue();
            if (currentIndex != null) {
                saveCurrentPage();
                viewModel.setCurrentPageIndex(currentIndex + 1);
            }
        });
    }

    private void createPrevNoteButton() {
        prevButton = findViewById(R.id.fab_prev_note);
        prevButton.setOnClickListener(view -> {
            Integer currentIndex = viewModel.getCurrentPageIndex().getValue();
            if (currentIndex != null) {
                saveCurrentPage();
                viewModel.setCurrentPageIndex(currentIndex - 1);
            }
        });
    }
    
    private void saveCurrentPage() {
        Integer index = viewModel.getCurrentPageIndex().getValue();
        if (index != null && viewModel.getSmartNotebook().getValue() != null) {
            AtomicNoteEntity note = viewModel.getSmartNotebook().getValue().getAtomicNotes().get(index);
            viewModel.saveCurrentPage(note);
        }
    }

    @Override
    public void onPageSave(AtomicNoteEntity note, int position) {
        viewModel.saveCurrentPage(note);
    }

    @Override
    public void onBackPressed() {
        viewModel.saveNoteTitle(noteTitleText.getText().toString());
        super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

