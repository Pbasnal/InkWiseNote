package com.originb.inkwisenote2.modules.smartnotes.ui;

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
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import com.originb.inkwisenote2.modules.smartnotes.ui.NoteDebugDialog;
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
    private ImageButton debugButton;
    private TextNotesDao textNotesDao;
    private TextNoteEntity textNoteEntity;

    public TextNoteFragment(SmartNotebook smartNotebook, AtomicNoteEntity atomicNote) {
        super(smartNotebook, atomicNote);
        textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.note_text_fragment, container, false);
        noteEditText = itemView.findViewById(R.id.note_edit_text);
        deleteBtn = itemView.findViewById(R.id.delete_note);
        debugButton = itemView.findViewById(R.id.debug_button);

        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View itemView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(itemView, savedInstanceState);
        deleteBtn.setOnClickListener(view ->
                EventBus.getDefault()
                        .post(new Events.DeleteNoteCommand(smartNotebook,
                                atomicNote))
        );

        debugButton.setOnClickListener(v -> {
            showDebugDialog();
        });

        loadNote();
    }

    /**
     * Show the debug dialog with note information
     */
    private void showDebugDialog() {
        if (getContext() != null) {
            NoteDebugDialog dialog = new NoteDebugDialog(getContext(), atomicNote, smartNotebook);
            dialog.show();
        }
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
