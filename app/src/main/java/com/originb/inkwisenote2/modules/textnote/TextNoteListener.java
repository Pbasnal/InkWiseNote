package com.originb.inkwisenote2.modules.textnote;

import com.google.android.gms.common.util.Strings;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TextNoteListener {

    private final TextNotesDao textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotebookDelete(Events.NotebookDeleted notebookToDelete) {
        SmartNotebook smartNotebook = notebookToDelete.smartNotebook;
        smartNotebook.atomicNotes.forEach(note ->
                {
                    textNotesDao.deleteTextNote(note.getNoteId());
                    deleteNoteMarkdown(note);
                }
        );
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNoteDelete(Events.NoteDeleted noteDeleted) {
        textNotesDao.deleteTextNote(noteDeleted.atomicNote.getNoteId());
        deleteNoteMarkdown(noteDeleted.atomicNote);
    }

    public void deleteNoteMarkdown(AtomicNoteEntity atomicNote) {
        if (Strings.isEmptyOrWhitespace(atomicNote.getFilepath())) return;
        Path path = Paths.get(atomicNote.getFilepath(), atomicNote.getFilename());
        Path pathWithExtension = Paths.get(path.toString() + ".md");
        try {
            Files.delete(pathWithExtension);
        } catch (Exception e) {
            System.out.println("Failed to delete the file: " + e.getMessage());
        }
    }
}
