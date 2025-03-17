package com.originb.inkwisenote2.modules.handwrittennotes

import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteDeleted
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NotebookDeleted
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.function.Consumer

class HandwrittenNoteEventListener {
    private val handwrittenNoteRepository: HandwrittenNoteRepository =
        Repositories.Companion.getInstance().getHandwrittenNoteRepository()

    init {
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNotebookDelete(notebookToDelete: NotebookDeleted) {
        val smartNotebook = notebookToDelete.smartNotebook
        smartNotebook!!.atomicNotes!!.forEach(
            Consumer { note: AtomicNoteEntity? -> handwrittenNoteRepository.deleteHandwrittenNote(note) }
        )
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNoteDelete(noteDeleted: NoteDeleted) {
        handwrittenNoteRepository.deleteHandwrittenNote(noteDeleted.atomicNote)
    }
}


