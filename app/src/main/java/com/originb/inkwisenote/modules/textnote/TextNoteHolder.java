package com.originb.inkwisenote.modules.textnote;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.activity.ComponentActivity;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.common.Routing;
import com.originb.inkwisenote.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote.modules.backgroundjobs.Events;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote.modules.smartnotes.ui.NoteHolder;
import com.originb.inkwisenote.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote.modules.textnote.data.TextNotesDao;
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
        BackgroundOps.execute(() -> getSmartNotebook(bookId),
                smartNotebookOpt -> {
                    notebook = smartNotebookOpt.get();
                    noteEditText.setText(textNoteEntity.getNoteText());
                    deleteBtn.setOnClickListener(view -> {
                        EventBus.getDefault().post(new Events.NotebookDeleted(notebook));
                        Routing.HomePageActivity.openHomePageAndStartFresh(parentActivity);
                    });
                });
    }

    private Optional<SmartNotebook> getSmartNotebook(long bookId) {
        Optional<SmartNotebook> notebookOpt = smartNotebookRepository.getSmartNotebooks(bookId);

        notebookOpt.ifPresent(notebook -> {
            textNoteEntity = textNotesDao.getTextNoteForBook(notebook.getSmartBook().getBookId());
            if (textNoteEntity == null) {
                textNoteEntity = new TextNoteEntity(notebook.getAtomicNotes().get(0).getNoteId(),
                        notebook.smartBook.getBookId());
                textNotesDao.insertTextNote(textNoteEntity);
            }
        });

        return notebookOpt;
    }

    @Override
    public boolean saveNote() {
        BackgroundOps.execute(() -> {
            textNoteEntity.setNoteText(noteEditText.getText().toString());
            textNotesDao.updateTextNote(textNoteEntity);
        });
        return true;
    }
}
