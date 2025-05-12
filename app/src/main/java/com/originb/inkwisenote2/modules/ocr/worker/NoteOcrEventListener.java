package com.originb.inkwisenote2.modules.ocr.worker;

import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus;
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
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
    public void onHandwrittenNoteSaved(Events.HandwrittenNoteSaved handwrittenNoteSaved) {
        long bookId = handwrittenNoteSaved.bookId;
        WorkManagerBus.scheduleWorkForTextParsingForBook(handwrittenNoteSaved.context,
                bookId,
                handwrittenNoteSaved.atomicNote.getNoteId());
        EventBus.getDefault().post(new Events.NoteStatus(handwrittenNoteSaved.bookId, TextProcessingStage.TEXT_PARSING));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTextNoteSaved(Events.TextNoteSaved textNoteSaved) {
        long bookId = textNoteSaved.bookId;
        WorkManagerBus.scheduleWorkForTextProcessingForBook(textNoteSaved.context, bookId, textNoteSaved.atomicNote.getNoteId());
        EventBus.getDefault().post(new Events.NoteStatus(textNoteSaved.bookId, TextProcessingStage.TEXT_PARSING));
    }
}


