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
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;
import org.greenrobot.eventbus.EventBus;

import java.util.Optional;

public class TextNoteFragment extends NoteFragment {
    private EditText noteEditText;
    private ImageButton deleteBtn;
    private ImageButton debugButton;

    private final TextNotesDao textNotesDao;
    private SmartNotebookRepository smartNotebookRepository;
    private TextNoteEntity textNoteEntity;
    private SmartNotebook notebook;

    public TextNoteFragment(SmartNotebook notebook, AtomicNoteEntity atomicNote) {
        super(notebook, atomicNote);
        textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.note_text_fragment, container, false);
        noteEditText = itemView.findViewById(R.id.note_edit_text);
        deleteBtn = itemView.findViewById(R.id.delete_note);
        debugButton = itemView.findViewById(R.id.debug_button);
        debugButton.setOnClickListener(v -> {
            showDebugDialog();
        });

        BackgroundOps.execute(() -> textNotesDao.getTextNoteForNote(atomicNote.getNoteId()),
                textNoteEntity -> {
                    if (textNoteEntity == null) return;
                    this.textNoteEntity = textNoteEntity;
                    noteEditText.setText(textNoteEntity.getNoteText());
                });

        deleteBtn.setOnClickListener(view ->
                EventBus.getDefault()
                        .post(new Events.DeleteNoteCommand(smartNotebook,
                                atomicNote))
        );

        return itemView;
    }

    private void showDebugDialog() {
        if (getContext() != null) {
            NoteDebugDialog dialog = new NoteDebugDialog(getContext(), atomicNote, smartNotebook);
            dialog.show();
        }
    }

    @Override
    public NoteHolderData getNoteHolderData() {
        String noteText = "";
        if (noteEditText != null) {
            noteText = noteEditText.getText().toString().trim();
        }

        return NoteHolderData.textNoteData(noteText);
    }
}
