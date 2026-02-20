package com.originb.inkwisenote2.modules.ocr.worker

import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.modules.backgroundjobs.Events.*
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.function.Consumer

class NoteOcrEventListener(private val noteOcrTextDao: NoteOcrTextsDao) {
    private val logger = Logger("NoteOcrEventListener")

    init {
        // Inject NoteOcrTextsDao via Koin
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNotebookDelete(notebookToDelete: NotebookDeleted) {
        val smartNotebook = notebookToDelete.smartNotebook
        smartNotebook!!.atomicNotes.forEach(Consumer { note: AtomicNoteEntity? -> noteOcrTextDao.deleteNoteText(note!!.noteId) }
        )
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNoteDelete(noteDeleted: NoteDeleted) {
        noteOcrTextDao.deleteNoteText(noteDeleted.atomicNote!!.noteId)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onHandwrittenNoteSaved(handwrittenNoteSaved: HandwrittenNoteSaved) {
        val bookId = handwrittenNoteSaved.bookId
        WorkManagerBus.scheduleWorkForTextParsingForBook(
            handwrittenNoteSaved.context!!,
            bookId,
            handwrittenNoteSaved.atomicNote!!.noteId
        )
        EventBus.getDefault().post(NoteStatus(handwrittenNoteSaved.bookId, TextProcessingStage.TEXT_PARSING))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTextNoteSaved(textNoteSaved: TextNoteSaved) {
        val bookId = textNoteSaved.bookId
        WorkManagerBus.scheduleWorkForTextProcessingForBook(
            textNoteSaved.context!!,
            bookId,
            textNoteSaved.atomicNote!!.noteId
        )
        EventBus.getDefault().post(NoteStatus(textNoteSaved.bookId, TextProcessingStage.TEXT_PARSING))
    }
}


