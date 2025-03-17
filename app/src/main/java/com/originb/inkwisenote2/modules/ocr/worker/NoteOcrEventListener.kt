package com.originb.inkwisenote2.modules.ocr.worker

import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.modules.backgroundjobs.Events.*
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.function.Consumer

class NoteOcrEventListener {
    private val logger = Logger("NoteOcrEventListener")

    private val noteOcrTextDao: NoteOcrTextDao =
        Repositories.Companion.getInstance().getNotesDb().noteOcrTextDao()

    init {
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNotebookDelete(notebookToDelete: NotebookDeleted) {
        val smartNotebook = notebookToDelete.smartNotebook
        smartNotebook!!.atomicNotes!!.forEach(
            Consumer { note: AtomicNoteEntity? -> noteOcrTextDao.deleteNoteText(note.getNoteId()) }
        )
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNoteDelete(noteDeleted: NoteDeleted) {
        noteOcrTextDao.deleteNoteText(noteDeleted.atomicNote.noteId)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSmartNotebookSaved(smartNotebookSaved: SmartNotebookSaved) {
        val bookId = smartNotebookSaved.smartNotebook!!.smartBook.bookId

        if (smartNotebookSaved.smartNotebook!!.atomicNotes!![0].noteType
            == NoteType.HANDWRITTEN_PNG.toString()
        ) {
            logger.debug("Scheduling text parsing work for bookId: $bookId")
            WorkManagerBus.scheduleWorkForTextParsingForBook(smartNotebookSaved.context, bookId)
            return
        } else {
            WorkManagerBus.scheduleWorkForTextProcessingForBook(smartNotebookSaved.context, bookId)
        }


        EventBus.getDefault().post(NoteStatus(smartNotebookSaved.smartNotebook, TextProcessingStage.TEXT_PARSING))
    }
}


