package com.originb.inkwisenote.modules.textnote;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.common.Routing;
import com.originb.inkwisenote.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote.modules.backgroundjobs.Events;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote.modules.textnote.data.TextNotesDao;
import org.greenrobot.eventbus.EventBus;

import java.util.Optional;

public class TextNoteActivity extends AppCompatActivity {
    private EditText noteEditText;
    private EditText noteTitle;
    private ImageButton deleteBtn;

    private String workingNotePath;
    private SmartNotebookRepository smartNotebookRepository;
    private TextNotesDao textNotesDao;

    private TextNoteEntity textNoteEntity;
    private SmartNotebook notebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_note);

        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();

        noteEditText = findViewById(R.id.note_edit_text);
        noteTitle = findViewById(R.id.text_note_title);
        deleteBtn = findViewById(R.id.delete_note);

        BackgroundOps.execute(this::getSmartNotebook,
                smartNotebook -> {
                    notebook = smartNotebook.get();
                    noteEditText.setText(textNoteEntity.getNoteText());
                    noteTitle.setText(notebook.smartBook.getTitle());

                    deleteBtn.setOnClickListener(view -> {
                        EventBus.getDefault().post(new Events.NotebookDeleted(notebook));
                        Routing.HomePageActivity.openHomePageAndStartFresh(this);
                    });
                });
    }

    private Optional<SmartNotebook> getSmartNotebook() {
        Long bookIdToOpen = getIntent().getLongExtra("bookId", -1);
        workingNotePath = getIntent().getStringExtra("workingNotePath");
        Optional<SmartNotebook> notebookOpt;
        if (bookIdToOpen != -1) {
            notebookOpt = smartNotebookRepository.getSmartNotebooks(bookIdToOpen);
        } else {
            notebookOpt = smartNotebookRepository.initializeNewSmartNotebook("",
                    workingNotePath,
                    NoteType.TEXT_NOTE);
        }

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
    public void onBackPressed() {
        super.onBackPressed();

        BackgroundOps.execute(() -> {
            textNoteEntity.setNoteText(noteEditText.getText().toString());
            textNotesDao.updateTextNote(textNoteEntity);
        });

        EventBus.getDefault().post(new Events.SmartNotebookSaved(notebook, this));
    }
}
