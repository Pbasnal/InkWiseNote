package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;

/**
 * Base fragment class for all note fragments
 */
public abstract class NoteFragment extends Fragment {

    private static final String ARG_NOTE_ID = "note_id";
    private static final String ARG_BOOK_ID = "book_id";

    protected SmartNotebookViewModel viewModel;
    protected SmartNotebookRepository smartNotebookRepository;
    protected long noteId;
    protected long bookId;
    protected AtomicNoteEntity atomicNote;

    /**
     * Set up bundle arguments for a note fragment
     * @param args Bundle to add arguments to
     * @param noteId The note ID
     * @param bookId The book ID
     */
    protected static void setArguments(Bundle args, long noteId, long bookId) {
        args.putLong(ARG_NOTE_ID, noteId);
        args.putLong(ARG_BOOK_ID, bookId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        
        // Get arguments
        if (getArguments() != null) {
            noteId = getArguments().getLong(ARG_NOTE_ID);
            bookId = getArguments().getLong(ARG_BOOK_ID);
        }
        
        // Get ViewModel from activity
        viewModel = new ViewModelProvider(requireActivity()).get(SmartNotebookViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Load the note
        loadNote();
    }

    /**
     * Load the note data
     */
    protected void loadNote() {
        // Each fragment will implement this to load its specific note data
    }

    /**
     * Get the note holder data for this fragment
     * @return The note holder data
     */
    public abstract NoteHolder.NoteHolderData getNoteHolderData();

    /**
     * Get the type of note this fragment handles
     * @return The note type
     */
    public abstract NoteType getNoteType();
} 