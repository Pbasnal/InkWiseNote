package com.originb.inkwisenote2.modules.textnote;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.activity.ComponentActivity;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.ui.NoteHolder;
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;
import org.greenrobot.eventbus.EventBus;

import java.util.Optional;

public class TextNoteHolder extends NoteHolder {
    private EditText noteEditText;
    private ImageButton deleteBtn;


    private TextNotesDao textNotesDao;

    private TextNoteEntity textNoteEntity;
    private SmartNotebook notebook;

    public TextNoteHolder(View itemView, ComponentActivity parentActivity, SmartNotebookRepository smartNotebookRepository) {
        super(itemView, parentActivity, smartNotebookRepository);

        noteEditText = itemView.findViewById(R.id.note_edit_text);
        deleteBtn = itemView.findViewById(R.id.delete_note);

        textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();
    }

    @Override
    public void setNote(long bookId, AtomicNoteEntity atomicNote) {
        BackgroundOps.execute(() -> getSmartNotebook(bookId, atomicNote.getNoteId()),
                smartNotebookOpt -> {
                    notebook = smartNotebookOpt.get();
                    noteEditText.setText(textNoteEntity.getNoteText());
                    deleteBtn.setOnClickListener(view -> {
                        EventBus.getDefault().post(new Events.NotebookDeleted(notebook));
                        Routing.HomePageActivity.openHomePageAndStartFresh(parentActivity);
                    });
                });
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
        return NoteHolderData.textNoteData(noteEditText.getText().toString().trim());
    }
}
