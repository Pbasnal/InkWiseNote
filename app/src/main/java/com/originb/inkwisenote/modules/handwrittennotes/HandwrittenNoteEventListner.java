package com.originb.inkwisenote.modules.handwrittennotes;

import com.originb.inkwisenote.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote.modules.backgroundjobs.Events;
import com.originb.inkwisenote.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HandwrittenNoteEventListner {

    private HandwrittenNoteRepository handwrittenNoteRepository;

    public HandwrittenNoteEventListner() {
        handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotebookDelete(Events.NotebookDeleted notebookToDelete) {
//        BackgroundOps.execute(() -> {
        SmartNotebook smartNotebook = notebookToDelete.smartNotebook;
        smartNotebook.atomicNotes.forEach(note ->
                handwrittenNoteRepository.deleteHandwrittenNote(note)
        );
//        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNoteDelete(Events.NoteDeleted noteDeleted) {
        handwrittenNoteRepository.deleteHandwrittenNote(noteDeleted.atomicNote);
    }
}


