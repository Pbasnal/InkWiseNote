package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import org.greenrobot.eventbus.EventBus;

/**
 * Fragment for initializing a note type
 */
public class InitNoteFragment extends NoteFragment {

    private final Logger logger = new Logger("InitNoteFragment");
    private CardView cardToHandwriting;
    private CardView cardToText;
    private ImageButton deleteNote;

    /**
     * Create a new instance of InitNoteFragment
     * @param noteId The note ID
     * @param bookId The book ID
     * @return A new instance of InitNoteFragment
     */
    public static InitNoteFragment newInstance(long noteId, long bookId) {
        InitNoteFragment fragment = new InitNoteFragment();
        Bundle args = new Bundle();
        setArguments(args, noteId, bookId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.note_init_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        cardToHandwriting = view.findViewById(R.id.touch_to_write);
        cardToText = view.findViewById(R.id.tap_to_text);
        deleteNote = view.findViewById(R.id.delete_note);

        cardToHandwriting.setOnClickListener(this::createHandwrittenNote);
        cardToText.setOnClickListener(this::createTextNote);
        
        deleteNote.setOnClickListener(v -> {
            BackgroundOps.execute(() -> {
                EventBus.getDefault().post(new Events.NoteDeleted(
                        smartNotebookRepository.getSmartNotebooks(bookId).get(),
                        viewModel.getNoteById(noteId)
                ));
            });
        });
        
        super.onViewCreated(view, savedInstanceState);
    }

    private void createTextNote(View view) {
        atomicNote = viewModel.getNoteById(noteId);
        if (atomicNote == null) return;
        
        BackgroundOps.execute(() -> {
            // Update note type in database
            atomicNote.setNoteType(NoteType.TEXT_NOTE.toString());
            viewModel.updateNoteType(atomicNote, NoteType.TEXT_NOTE);
        });
    }

    private void createHandwrittenNote(View view) {
        atomicNote = viewModel.getNoteById(noteId);
        if (atomicNote == null) return;
        
        BackgroundOps.execute(() -> {
            // Update note type in database
            atomicNote.setNoteType(NoteType.HANDWRITTEN_PNG.toString());
            viewModel.updateNoteType(atomicNote, NoteType.HANDWRITTEN_PNG);
        });
    }

    @Override
    protected void loadNote() {
        atomicNote = viewModel.getNoteById(noteId);
        logger.debug("Setting init note");
    }

    @Override
    public NoteHolder.NoteHolderData getNoteHolderData() {
        return NoteHolder.NoteHolderData.initNoteData();
    }

    @Override
    public NoteType getNoteType() {
        return NoteType.NOT_SET;
    }
} 