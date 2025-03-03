package com.originb.inkwisenote.modules.noterelation;

import androidx.lifecycle.LifecycleOwner;
import com.originb.inkwisenote.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote.modules.backgroundjobs.Events;
import com.originb.inkwisenote.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote.modules.repositories.NoteRelationRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class NoteRelationEventListner {
    private NoteRelationRepository noteRelationRepository;

    public NoteRelationEventListner() {
        noteRelationRepository = Repositories.getInstance().getNoteRelationRepository();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onDataUpdateEvent(Events.NotebookDeleted notebookToDelete) {
        BackgroundOps.execute(() -> {
            SmartNotebook smartNotebook = notebookToDelete.smartNotebook;
            smartNotebook.atomicNotes.forEach(note ->
                    noteRelationRepository.deleteNoteRelationData(note)
            );
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNoteDelete(Events.NoteDeleted noteDeleted) {
        BackgroundOps.execute(() -> BackgroundOps.execute(() ->
                noteRelationRepository.deleteNoteRelationData(noteDeleted.atomicNote)
        ));
    }
}
