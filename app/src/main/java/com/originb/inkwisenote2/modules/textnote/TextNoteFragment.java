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
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import com.originb.inkwisenote2.modules.smartnotes.ui.NoteFragment;
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

    public TextNoteFragment(SmartNotebook smartNotebook, AtomicNoteEntity atomicNote) {
        super(smartNotebook, atomicNote);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();
        textNoteEntity = textNotesDao.getTextNoteForNote(atomicNote.getNoteId());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.note_text_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        noteEditText = view.findViewById(R.id.note_edit_text);
        deleteBtn = view.findViewById(R.id.delete_note);

        deleteBtn.setOnClickListener(v -> {
            BackgroundOps.execute(() -> {
                EventBus.getDefault().post(new Events.NoteDeleted(
                        smartNotebook,
                        atomicNote
                ));
            });
        });

        loadNote();
        super.onViewCreated(view, savedInstanceState);
    }

    protected void loadNote() {
        BackgroundOps.execute(() -> {
            // Get or create the text note entity
            textNoteEntity = textNotesDao.getTextNoteForNote(atomicNote.getNoteId());
            if (textNoteEntity == null) {
                textNoteEntity = new TextNoteEntity(atomicNote.getNoteId(), smartNotebook.smartBook.getBookId());
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
    public NoteHolderData getNoteHolderData() {
        String text = noteEditText != null ? noteEditText.getText().toString().trim() : "";
        return NoteHolderData.textNoteData(text);
    }
} 