package com.originb.inkwisenote2.modules.smartnotes

import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteDeleted
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NotebookDeleted
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SmartNotebookEventListener {
    private val smartNotebookRepository: SmartNotebookRepository =
        Repositories.Companion.getInstance().getSmartNotebookRepository()

    init {
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNotebookDelete(notebookToDelete: NotebookDeleted) {
        BackgroundOps.Companion.execute(Runnable {
            BackgroundOps.Companion.execute(
                Runnable { smartNotebookRepository.deleteSmartNotebook(notebookToDelete.smartNotebook) }
            )
        })
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNoteDelete(noteDeleted: NoteDeleted) {
        BackgroundOps.Companion.execute(Runnable {
            BackgroundOps.Companion.execute(
                Runnable {
                    smartNotebookRepository.deleteNoteFromBook(
                        noteDeleted.smartNotebook,
                        noteDeleted.atomicNote
                    )
                }
            )
        })
    }
}
