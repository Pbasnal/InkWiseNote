package com.originb.inkwisenote.modules.ocr.worker;

import com.originb.inkwisenote.common.Logger;
import com.originb.inkwisenote.modules.backgroundjobs.Events;
import com.originb.inkwisenote.modules.backgroundjobs.WorkManagerBus;
import com.originb.inkwisenote.modules.noterelation.data.TextProcessingStage;
import com.originb.inkwisenote.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.smartnotes.data.NoteType;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class NoteOcrEventListener {

    private Logger logger = new Logger("NoteOcrEventListener");

    private NoteOcrTextDao noteOcrTextDao;

    public NoteOcrEventListener() {
        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotebookDelete(Events.NotebookDeleted notebookToDelete) {
        SmartNotebook smartNotebook = notebookToDelete.smartNotebook;
        smartNotebook.atomicNotes.forEach(note ->
                noteOcrTextDao.deleteNoteText(note.getNoteId())
        );
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNoteDelete(Events.NoteDeleted noteDeleted) {
        noteOcrTextDao.deleteNoteText(noteDeleted.atomicNote.getNoteId());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSmartNotebookSaved(Events.SmartNotebookSaved smartNotebookSaved) {
        long bookId = smartNotebookSaved.smartNotebook.smartBook.getBookId();

        if (smartNotebookSaved.smartNotebook.atomicNotes.get(0).getNoteType()
                .equals(NoteType.HANDWRITTEN_PNG.toString())) {
            logger.debug("Scheduling text parsing work for bookId: " + bookId);
            WorkManagerBus.scheduleWorkForTextParsingForBook(smartNotebookSaved.context, bookId);
            return;
        } else {
            WorkManagerBus.scheduleWorkForTextProcessingForBook(smartNotebookSaved.context, bookId);
        }


        EventBus.getDefault().post(new Events.NoteStatus(smartNotebookSaved.smartNotebook, TextProcessingStage.TEXT_PARSING));
    }
}


