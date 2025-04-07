package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.DateTimeUtils;
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPage;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Optional;

public class SmartNotebookActivity extends AppCompatActivity {

    private Logger logger = new Logger("SmartNotebookActivity");

    private String workingNotePath;
    private int indexOfCurrentPage;

    private SmartNotebook smartNotebook;
    private SmartNotebookRepository smartNotebookRepository;
    private AtomicNotesDomain atomicNotesDomain;

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

        createNoteTitleEditText();
        createNoteCreatedTimeText();

        this.indexOfCurrentPage = 0;
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        atomicNotesDomain = Repositories.getInstance().getAtomicNotesDomain();

        recyclerView = findViewById(R.id.smart_note_page_view);
        scrollLayout = new SmartNotebookPageScrollLayout(this);
        recyclerView.addOnScrollListener(new SmartNotebookScrollListener(scrollLayout));
        recyclerView.setLayoutManager(scrollLayout);

        smartNotebookAdapter = new SmartNotebookAdapter(this, null);
        BackgroundOps.executeOpt(this::getSmartNotebook, smartNotebook -> {
            this.smartNotebook = smartNotebook;
            smartNotebookAdapter.setSmartNotebook(smartNotebook);
            noteTitleText.setText(smartNotebook.smartBook.getTitle());
            noteCreatedTime.setText(DateTimeUtils.msToDateTime(smartNotebook.smartBook.getCreatedTimeMillis()));
            recyclerView.setAdapter(smartNotebookAdapter);

            if (smartNotebook.getAtomicNotes().size() > 1) {
                nextButton.setVisibility(View.VISIBLE);
            }
            updatePageNumberText(smartNotebook);
        });

        createNextNoteButton();
        createPrevNoteButton();
        createNewNoteButton();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteDelete(Events.NoteDeleted noteDeleted) {
        long noteId = noteDeleted.atomicNote.getNoteId();
        smartNotebook.removeNote(noteId);

        if (smartNotebook.atomicNotes.isEmpty()) {
            BackgroundOps.execute(() -> smartNotebookRepository.deleteSmartNotebook(smartNotebook),
                    () -> Routing.HomePageActivity.openHomePageAndStartFresh(this));
        }

        smartNotebookAdapter.removeNoteCard(noteId);
        updatePageNumberText(smartNotebook);
    }

    private void updatePageNumberText(SmartNotebook smartNotebook) {
        pageNumText = findViewById(R.id.page_num_text);
        setPageNum(indexOfCurrentPage);
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
        newNotePageBtn.setOnClickListener(this::onNewNotePageClick);
    }

    private void onNewNotePageClick(View view) {
        // Determine the position to insert the new item after the currently visible one
        int positionOfNewHolder = indexOfCurrentPage + 1;
        BackgroundOps.execute(() -> {
                    AtomicNoteEntity newAtomicNote = atomicNotesDomain.saveAtomicNote(AtomicNotesDomain.constructAtomicNote(
                            "",
                            workingNotePath,
                            NoteType.NOT_SET));
                    SmartBookPage newSmartPage = smartNotebookRepository.newSmartBookPage(smartNotebook.smartBook,
                            newAtomicNote, positionOfNewHolder - 1);
                    smartNotebook.insertAtomicNoteAndPage(positionOfNewHolder - 1, newAtomicNote, newSmartPage);

                    smartNotebookAdapter.saveNotebookPageAt(indexOfCurrentPage, newAtomicNote);
                    return smartNotebook;
                },
                atomicNoteEntity -> {
                    // Notify the adapter about the new item inserted
                    smartNotebookAdapter.notifyItemInserted(positionOfNewHolder);
                    int totalItemCount = recyclerView.getAdapter().getItemCount();
                    if (positionOfNewHolder == totalItemCount) {
                        nextButton.setVisibility(View.INVISIBLE);
                    } else {
                        nextButton.setVisibility(View.VISIBLE);
                    }

                    prevButton.setVisibility(View.VISIBLE);

                    scrollLayout.setScrollRequested(true);
                    // Optionally scroll to the new item
                    recyclerView.postDelayed(() -> {
                        recyclerView.smoothScrollToPosition(positionOfNewHolder);
                        setPageNum(positionOfNewHolder);
                        indexOfCurrentPage = positionOfNewHolder;
                    }, 10);
                });
    }

    private void createNextNoteButton() {
        nextButton = findViewById(R.id.fab_next_note);

        nextButton.setVisibility(View.INVISIBLE);
        nextButton.setOnClickListener(view -> {
            // Get the total number of items
            int totalItemCount = recyclerView.getAdapter().getItemCount();

            smartNotebookAdapter.saveNotebookPageAt(indexOfCurrentPage,
                    smartNotebook.getAtomicNotes().get(indexOfCurrentPage));

            int pageIndex = indexOfCurrentPage + 1;

            // hide next button if this is the last visible note
            if (pageIndex >= totalItemCount - 1) {
                nextButton.setVisibility(View.INVISIBLE);
            }
            if (totalItemCount > 1) {
                prevButton.setVisibility(View.VISIBLE);
            }

            // Check if we can scroll to the next item
            if (pageIndex < totalItemCount) {
                // Scroll to the next item
                scrollLayout.setScrollRequested(true);
                recyclerView.smoothScrollToPosition(pageIndex);
                setPageNum(pageIndex);
                indexOfCurrentPage = pageIndex;
            }
            // don't use currentPageIndex after this because it is getting modified above
        });
    }

    private void createPrevNoteButton() {
        prevButton = findViewById(R.id.fab_prev_note);
        prevButton.setVisibility(View.INVISIBLE);
        prevButton.setOnClickListener(view -> {
            // Get the total number of items
            int totalItemCount = recyclerView.getAdapter().getItemCount();

            smartNotebookAdapter.saveNotebookPageAt(indexOfCurrentPage,
                    smartNotebook.getAtomicNotes().get(indexOfCurrentPage));

            int indexOfPrevPage = indexOfCurrentPage - 1;

            if (indexOfPrevPage <= 0) {
                prevButton.setVisibility(View.INVISIBLE);
            }
            if (totalItemCount > 1) {
                nextButton.setVisibility(View.VISIBLE);
            }
            // Check if we can scroll to the next item
            if (indexOfPrevPage >= 0) {
                // Scroll to the next item
                scrollLayout.setScrollRequested(true);
                recyclerView.smoothScrollToPosition(indexOfPrevPage);
                setPageNum(indexOfPrevPage);
                indexOfCurrentPage = indexOfPrevPage;
            }
            // don't use currentPageIndex after this because it is getting modified above
        });
    }

    private Optional<SmartNotebook> getSmartNotebook() {
        Long bookIdToOpen = getIntent().getLongExtra("bookId", -1);
        workingNotePath = getIntent().getStringExtra("workingNotePath");

        if (bookIdToOpen != -1) {
            return smartNotebookRepository.getSmartNotebooks(bookIdToOpen);
        }

        return smartNotebookRepository.initializeNewSmartNotebook("",
                workingNotePath,
                NoteType.NOT_SET);
    }

    private void setPageNum(int position) {
        pageNumText.setText(new StringBuilder()
                .append(position + 1)
                .append("/")
                .append(smartNotebook.getAtomicNotes().size())
                .toString());
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        smartNotebookAdapter.saveNote(noteTitleText.getText().toString());
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

