package com.originb.inkwisenote2.modules.noterelation

import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteDeleted
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NotebookDeleted
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.function.Consumer

class NoteRelationEventListener(private val noteRelationRepository: NoteRelationRepository) {
    init {
        // Inject NoteRelationRepository via Koin
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onDataUpdateEvent(notebookToDelete: NotebookDeleted) {
        execute {
            val smartNotebook = notebookToDelete.smartNotebook
            smartNotebook.atomicNotes.forEach(Consumer { note: AtomicNoteEntity ->
                noteRelationRepository.deleteNoteRelationData(
                    note
                )
            }
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNoteDelete(noteDeleted: NoteDeleted) {
        execute(Runnable {
            execute(Runnable { noteRelationRepository.deleteNoteRelationData(noteDeleted.atomicNote) }
            )
        })
    }
}
