package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.functionalUtils.Try;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;
import org.greenrobot.eventbus.EventBus;

import java.util.Optional;

public class TextNoteFragment extends NoteFragment {
    private EditText noteEditText;
    private ImageButton deleteBtn;

    private final TextNotesDao textNotesDao;
    private SmartNotebookRepository smartNotebookRepository;
    private TextNoteEntity textNoteEntity;
    private SmartNotebook notebook;

    public TextNoteFragment() {
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

        BackgroundOps.execute(() -> getSmartNotebook(bookId, atomicNote.getNoteId()),
                smartNotebookOpt -> {
                    notebook = smartNotebookOpt.get();
                    noteEditText.setText(textNoteEntity.getNoteText());
                    deleteBtn.setOnClickListener(view ->
                            EventBus.getDefault()
                                    .post(new Events.DeleteNoteCommand(notebook,
                                            atomicNote))
                    );
                });

        return itemView;
    }

    private Optional<SmartNotebook> getSmartNotebook(long bookId, long noteId) {
        Optional<SmartNotebook> notebookOpt = smartNotebookRepository.getSmartNotebooks(bookId);

        notebookOpt.ifPresent(notebook -> {
            textNoteEntity = textNotesDao.getTextNoteForNote(noteId);
            if (textNoteEntity == null) {
                textNoteEntity = new TextNoteEntity(
                        noteId,
                        notebook.smartBook.getBookId());
                textNotesDao.insertTextNote(textNoteEntity);
            }
        });

        return notebookOpt;
    }


    @Override
    public NoteHolderData getNoteHolderData() {
        String noteText = "";
        if (noteEditText != null) {
            noteEditText.getText().toString();
            noteText = noteEditText.getText().toString().trim();
        }

        return NoteHolderData.textNoteData(noteText);
    }
}
