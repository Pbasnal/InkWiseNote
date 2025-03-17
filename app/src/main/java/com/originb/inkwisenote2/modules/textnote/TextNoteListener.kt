package com.originb.inkwisenote2.modules.textnote

import com.originb.inkwisenote2.modules.backgroundjobs.Events.NotebookDeleted
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TextNoteListener {
    private val textNotesDao: TextNotesDao = Repositories.Companion.getInstance().getNotesDb().textNotesDao()

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNotebookDelete(notebookToDelete: NotebookDeleted) {
        val smartNotebook = notebookToDelete.smartNotebook
        textNotesDao.deleteTextNotes(smartNotebook.getSmartBook().bookId)
    }
}
