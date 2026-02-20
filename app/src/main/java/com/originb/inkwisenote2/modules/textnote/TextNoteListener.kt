package com.originb.inkwisenote2.modules.textnote

import com.google.android.gms.common.util.Strings
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteDeleted
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NotebookDeleted
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Consumer

class TextNoteListener(private val textNotesDao: TextNotesDao) {
    init {
        // Inject TextNotesDao via Koin
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNotebookDelete(notebookToDelete: NotebookDeleted) {
        val smartNotebook = notebookToDelete.smartNotebook
        smartNotebook!!.atomicNotes.forEach(Consumer { note: AtomicNoteEntity? ->
            textNotesDao.deleteTextNote(note!!.getNoteId())
            deleteNoteMarkdown(note)
        }
        )
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNoteDelete(noteDeleted: NoteDeleted) {
        textNotesDao.deleteTextNote(noteDeleted.atomicNote!!.getNoteId())
        deleteNoteMarkdown(noteDeleted.atomicNote!!)

        val notebookDir = File(noteDeleted.atomicNote!!.getFilepath())
        if (notebookDir.exists() && notebookDir.isDirectory()) {
            // Delete all files in the directory
            val files = notebookDir.listFiles()
            if (files == null || files.size == 0) {
                notebookDir.delete()
            }
        }
    }

    fun deleteNoteMarkdown(atomicNote: AtomicNoteEntity) {
        if (Strings.isEmptyOrWhitespace(atomicNote.getFilepath())) return
        val path = Paths.get(atomicNote.getFilepath(), atomicNote.getFilename())
        val pathWithExtension = Paths.get(path.toString() + ".md")
        try {
            Files.delete(pathWithExtension)
        } catch (e: Exception) {
            println("Failed to delete the file: " + e.message)
        }
    }
}
