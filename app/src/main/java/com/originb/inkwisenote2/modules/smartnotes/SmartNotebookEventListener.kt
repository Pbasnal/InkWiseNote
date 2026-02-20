package com.originb.inkwisenote2.modules.smartnotes

import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import com.originb.inkwisenote2.modules.backgroundjobs.Events.DeleteNoteCommand
import com.originb.inkwisenote2.modules.backgroundjobs.Events.DeleteNotebookCommand
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SmartNotebookEventListener(private val smartNotebookRepository: SmartNotebookRepository) {
    init {
        // Inject SmartNotebookRepository via Koin
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onDeleteNotebookCommand(deleteNotebookCommand: DeleteNotebookCommand) {
        execute(Runnable {
            execute(Runnable { smartNotebookRepository.deleteSmartNotebook(deleteNotebookCommand.smartNotebook!!) }
            )
        })
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onDeleteNoteCommand(deleteNoteCommand: DeleteNoteCommand) {
        execute(Runnable {
            execute(Runnable {
                smartNotebookRepository.deleteNoteFromBook(
                    deleteNoteCommand.smartNotebook!!,
                    deleteNoteCommand.atomicNote!!
                )
            }
            )
        })
    }
}
