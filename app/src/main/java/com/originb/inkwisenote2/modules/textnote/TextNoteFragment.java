package com.originb.inkwisenote2.modules.textnote;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote2.modules.smartnotes.ui.NoteFragment;
import com.originb.inkwisenote2.modules.smartnotes.ui.NoteHolder;
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;

import org.greenrobot.eventbus.EventBus;

/**
 * Fragment for displaying and editing text notes
 */
public class TextNoteFragment extends NoteFragment {

    private EditText noteEditText;
    private ImageButton deleteBtn;
    private TextNotesDao textNotesDao;
    private TextNoteEntity textNoteEntity;

    /**
     * Create a new instance of TextNoteFragment
     * @param noteId The note ID
     * @param bookId The book ID
     * @return A new instance of TextNoteFragment
     */
    public static TextNoteFragment newInstance(long noteId, long bookId) {
        TextNoteFragment fragment = new TextNoteFragment();
        Bundle args = new Bundle();
        setArguments(args, noteId, bookId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.note_text_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        noteEditText = view.findViewById(R.id.note_edit_text);
        deleteBtn = view.findViewById(R.id.delete_note);
        
        deleteBtn.setOnClickListener(v -> {
            BackgroundOps.execute(() -> {
                EventBus.getDefault().post(new Events.NoteDeleted(
                        smartNotebookRepository.getSmartNotebooks(bookId).get(),
                        viewModel.getNoteById(noteId)
                ));
            });
        });
        
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void loadNote() {
        BackgroundOps.execute(() -> {
            // Get the atomic note from the view model
            atomicNote = viewModel.getNoteById(noteId);
            
            // Get or create the text note entity
            textNoteEntity = textNotesDao.getTextNoteForNote(noteId);
            if (textNoteEntity == null) {
                textNoteEntity = new TextNoteEntity(noteId, bookId);
                textNotesDao.insertTextNote(textNoteEntity);
                return "";
            }
            
            return textNoteEntity.getNoteText();
        }, noteText -> {
            if (noteEditText != null) {
                noteEditText.setText(noteText);
            }
        });
    }

    @Override
    public NoteHolder.NoteHolderData getNoteHolderData() {
        String text = noteEditText != null ? noteEditText.getText().toString().trim() : "";
        return NoteHolder.NoteHolderData.textNoteData(text);
    }

    @Override
    public NoteType getNoteType() {
        return NoteType.TEXT_NOTE;
    }
} 