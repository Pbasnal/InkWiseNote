package com.originb.inkwisenote2.modules.noterelation

import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteDeleted
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NotebookDeleted
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.function.Consumer

class NoteRelationEventListener {
    private val noteRelationRepository: NoteRelationRepository =
        Repositories.Companion.getInstance().getNoteRelationRepository()

    init {
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onDataUpdateEvent(notebookToDelete: NotebookDeleted) {
        BackgroundOps.Companion.execute(Runnable {
            val smartNotebook = notebookToDelete.smartNotebook
            smartNotebook!!.atomicNotes!!.forEach(
                Consumer { note: AtomicNoteEntity? -> noteRelationRepository.deleteNoteRelationData(note) }
            )
        })
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNoteDelete(noteDeleted: NoteDeleted) {
        BackgroundOps.Companion.execute(Runnable {
            BackgroundOps.Companion.execute(
                Runnable { noteRelationRepository.deleteNoteRelationData(noteDeleted.atomicNote) }
            )
        })
    }
}
