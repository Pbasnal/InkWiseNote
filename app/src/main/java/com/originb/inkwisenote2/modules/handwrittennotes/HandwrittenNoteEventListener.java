package com.originb.inkwisenote2.modules.handwrittennotes;

import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.koin.java.KoinJavaComponent;

public class HandwrittenNoteEventListener {

    private HandwrittenNoteRepository handwrittenNoteRepository;

    public HandwrittenNoteEventListener(HandwrittenNoteRepository handwrittenNoteRepository) {
        this.handwrittenNoteRepository = handwrittenNoteRepository;
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotebookDelete(Events.NotebookDeleted notebookToDelete) {
        SmartNotebook smartNotebook = notebookToDelete.smartNotebook;
        smartNotebook.atomicNotes.forEach(note ->
                {
                    handwrittenNoteRepository.deleteHandwrittenNote(note);
                }
        );
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNoteDelete(Events.NoteDeleted noteDeleted) {
        handwrittenNoteRepository.deleteHandwrittenNote(noteDeleted.atomicNote);
    }
}


