package com.originb.inkwisenote.ux.activities.smartnotebook;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote.Logger;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.commonutils.DateTimeUtils;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookPage;
import com.originb.inkwisenote.modules.backgroundworkers.WorkManagerBus;
import com.originb.inkwisenote.modules.messaging.BackgroundOps;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;

import java.util.Optional;

public class SmartNotebookActivity extends AppCompatActivity {

    private Logger logger = new Logger("SmartNotebookActivity");

    private String workingNotePath;

    private SmartNotebook smartNotebook;
    private SmartNotebookRepository smartNotebookRepository;

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

        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();

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
            createPageNumberText(smartNotebook);
        });

        createNextNoteButton();
        createPrevNoteButton();
        createNewNoteButton();
    }

    private void createPageNumberText(SmartNotebook smartNotebook) {
        pageNumText = findViewById(R.id.page_num_text);
        pageNumText.setText("1/" + smartNotebook.atomicNotes.size());
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
        int currentVisibleItemIndex = scrollLayout.findLastVisibleItemPosition();

        // Determine the position to insert the new item after the currently visible one
        int newPosition = currentVisibleItemIndex + 1;
        BackgroundOps.execute(() -> {
                    AtomicNoteEntity newAtomicNote = smartNotebookRepository.newHandwrittenNote("", workingNotePath);
                    SmartBookPage newSmartPage = smartNotebookRepository.newSmartBookPage(smartNotebook.smartBook,
                            newAtomicNote, newPosition);
                    smartNotebook.insertAtomicNoteAndPage(newPosition, newAtomicNote, newSmartPage);

                    smartNotebookAdapter.saveNotebookPageAt(currentVisibleItemIndex, newAtomicNote);
                    return smartNotebook;
                },
                atomicNoteEntity -> {
                    // Notify the adapter about the new item inserted
                    smartNotebookAdapter.notifyItemInserted(newPosition);

                    scrollLayout.setScrollRequested(true);
                    // Optionally scroll to the new item
                    recyclerView.post(() -> {
                        recyclerView.smoothScrollToPosition(newPosition);
                        pageNumText.setText(newPosition + "/" + smartNotebook.getAtomicNotes().size());
                    });

                    int totalItemCount = recyclerView.getAdapter().getItemCount();
                    if (newPosition == totalItemCount - 1) {
                        nextButton.setVisibility(View.INVISIBLE);
                    } else {
                        nextButton.setVisibility(View.VISIBLE);
                    }

                    prevButton.setVisibility(View.VISIBLE);
                });
    }

    private void createNextNoteButton() {
        nextButton = findViewById(R.id.fab_next_note);

        nextButton.setVisibility(View.INVISIBLE);
        nextButton.setOnClickListener(view -> {
            // Get the total number of items
            int totalItemCount = recyclerView.getAdapter().getItemCount();

            // Get the last visible item position
            int lastVisibleItemPosition = scrollLayout.findLastVisibleItemPosition();

            smartNotebookAdapter.saveNotebookPageAt(lastVisibleItemPosition,
                    smartNotebook.getAtomicNotes().get(lastVisibleItemPosition));

            int nextPosition = lastVisibleItemPosition + 1;
            // Check if we can scroll to the next item
            if (nextPosition < totalItemCount) {
                // Scroll to the next item
                scrollLayout.setScrollRequested(true);
                recyclerView.smoothScrollToPosition(nextPosition);
                pageNumText.setText(nextPosition + "/" + smartNotebook.getAtomicNotes().size());
            }

            // hide next button if this is the last visible note
            if (nextPosition >= totalItemCount - 1) {
                nextButton.setVisibility(View.INVISIBLE);
            }
            if (totalItemCount > 1) {
                prevButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void createPrevNoteButton() {
        prevButton = findViewById(R.id.fab_prev_note);
        prevButton.setVisibility(View.INVISIBLE);
        prevButton.setOnClickListener(view -> {
            // Get the total number of items
            int totalItemCount = recyclerView.getAdapter().getItemCount();

            // Get the last visible item position
            int lastVisibleItemPosition = scrollLayout.findLastVisibleItemPosition();

            smartNotebookAdapter.saveNotebookPageAt(lastVisibleItemPosition,
                    smartNotebook.getAtomicNotes().get(lastVisibleItemPosition));

            int prevPosition = lastVisibleItemPosition - 1;
            // Check if we can scroll to the next item
            if (prevPosition >= 0) {
                // Scroll to the next item
                scrollLayout.setScrollRequested(true);
                recyclerView.smoothScrollToPosition(prevPosition);
                pageNumText.setText((prevPosition + 1) + "/" + smartNotebook.getAtomicNotes().size());
            }
            if (prevPosition <= 0) {
                prevButton.setVisibility(View.INVISIBLE);
            }
            if (totalItemCount > 1) {
                nextButton.setVisibility(View.VISIBLE);
            }
        });
    }


    private Optional<SmartNotebook> getSmartNotebook() {
        Long noteIdToOpen = getIntent().getLongExtra("noteId", -1);
        Long bookIdToOpen = getIntent().getLongExtra("bookId", -1);
        workingNotePath = getIntent().getStringExtra("workingNotePath");

        if (bookIdToOpen != -1) {
            return smartNotebookRepository.getSmartNotebook(bookIdToOpen);
        }
        if (noteIdToOpen != -1) {
            return smartNotebookRepository.getSmartNotebookContainingNote(noteIdToOpen);
        }

        return smartNotebookRepository.initializeNewSmartNotebook("",
                workingNotePath);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
//        smartNotebookAdapter.saveNote(noteTitleText.getText().toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        smartNotebookAdapter.saveNote(noteTitleText.getText().toString());
    }

    @Override
    public void onStop() {
        super.onStop();
        // This needs a thoughtful approach
//        WorkManagerBus.scheduleWorkForTextParsingForBook(this, smartNotebook.getSmartBook().getBookId());
    }
}

