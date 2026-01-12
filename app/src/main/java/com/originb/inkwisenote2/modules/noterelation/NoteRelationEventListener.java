package com.originb.inkwisenote2.modules.noterelation;

import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.koin.java.KoinJavaComponent;

public class NoteRelationEventListener {
    private NoteRelationRepository noteRelationRepository;

    public NoteRelationEventListener(NoteRelationRepository noteRelationRepository) {
        // Inject NoteRelationRepository via Koin
        this.noteRelationRepository = noteRelationRepository;
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
