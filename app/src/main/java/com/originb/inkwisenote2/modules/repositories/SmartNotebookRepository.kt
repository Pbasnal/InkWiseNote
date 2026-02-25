package com.originb.inkwisenote2.modules.repositories

import android.app.Application
import android.content.Context
import com.originb.inkwisenote2.common.ListUtils
import com.originb.inkwisenote2.common.isNullOrWhitespace
import com.originb.inkwisenote2.modules.backgroundjobs.Events.*
import com.originb.inkwisenote2.modules.smartnotes.data.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.nio.file.Paths
import java.util.*

class SmartNotebookRepository(
    private val atomicNotesDomain: AtomicNotesDomain,
    private val atomicNoteEntitiesDao: AtomicNoteEntitiesDao,
    private val smartBooksDao: SmartBooksDao,
    private val smartBookPagesDao: SmartBookPagesDao
) {
    // Create the data and return the notebook entity
    fun initializeNewSmartNotebook(
        title: String,
        directoryPath: String,
        noteType: NoteType
    ): SmartNotebook {
        // Create a directory with the notebook title
        val notebookDirectory = Paths.get(directoryPath, title).toString()
        val notebookDir = File(notebookDirectory)
        if (!notebookDir.exists()) {
            notebookDir.mkdirs()
        }

        val atomicNoteEntity = atomicNotesDomain.saveAtomicNote(
            AtomicNotesDomain.Companion.constructAtomicNote(
                "",
                notebookDirectory,
                noteType
            )
        )
        val smartBookEntity = newSmartBook(title, atomicNoteEntity.createdTimeMillis)
        val smartBookPage = newSmartBookPage(smartBookEntity, atomicNoteEntity, 0)
        val smartNotebook = SmartNotebook(smartBookEntity, smartBookPage, atomicNoteEntity)
        return smartNotebook
    }

    fun deleteSmartNotebook(smartNotebook: SmartNotebook) {
        // Delete notebook folder from filesystem if it exists
        try {
            if (smartNotebook.atomicNotes.isNotEmpty()) {
                val firstNote = smartNotebook.atomicNotes[0]
                if (firstNote.filepath != null) {
                    val notebookDir = File(firstNote.filepath)
                    if (notebookDir.exists() && notebookDir.isDirectory()) {
                        // Delete all files in the directory
                        val files = notebookDir.listFiles()
                        if (files != null) {
                            for (file in files) {
                                file.delete()
                            }
                        }
                        // Delete the directory itself
                        notebookDir.delete()
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but continue with database deletion
            System.err.println("Error deleting notebook directory: " + e.message)
        }

        // Delete database records
        smartNotebook.smartBook?.let { smartBookPagesDao.deleteSmartBookPages(it.bookId) }

        for (note in smartNotebook.atomicNotes) {
            atomicNoteEntitiesDao.deleteAtomicNote(note.noteId)
        }

        smartNotebook.smartBook?.let { smartBooksDao.deleteSmartBook(it.bookId) }

        EventBus.getDefault().post(NotebookDeleted(smartNotebook))
    }

    fun deleteNoteFromBook(smartNotebook: SmartNotebook, atomicNote: AtomicNoteEntity) {
        if (smartNotebook.atomicNotes.size <= 1) deleteSmartNotebook(smartNotebook)
        else {
            smartBookPagesDao.deleteNotePages(atomicNote.noteId)
            atomicNoteEntitiesDao.deleteAtomicNote(atomicNote.noteId)

            EventBus.getDefault().post(NoteDeleted(smartNotebook, atomicNote))
        }
    }

    fun updateNotebook(smartNotebook: SmartNotebook, context: Context) {
        val updateTime = System.currentTimeMillis()

        val smartBookEntity = smartNotebook.smartBook ?: return

        if (smartBookEntity.bookId == -1L) return

        smartBookEntity.lastModifiedTimeMillis = System.currentTimeMillis()
        smartBooksDao.updateSmartBook(smartBookEntity)

        for (atomicNote in smartNotebook.atomicNotes) {
            atomicNote.lastModifiedTimeMillis = updateTime
        }
        atomicNotesDomain.updateAtomicNotes(smartNotebook.atomicNotes)

        smartBookPagesDao.updateSmartBookPage(smartNotebook.smartBookPages)
        EventBus.getDefault().post(SmartNotebookSaved(smartNotebook, context))
    }

    fun saveSmartNotebook(smartNotebook: SmartNotebook, context: Application?): SmartNotebook {
        val updateTime = System.currentTimeMillis()
        val smartBookEntity = smartNotebook.smartBook ?: return smartNotebook
        if (smartBookEntity.bookId > -1) return smartNotebook

        smartBookEntity.createdTimeMillis = updateTime
        smartBookEntity.lastModifiedTimeMillis = updateTime

        val bookId = smartBooksDao.insertSmartBook(smartBookEntity)
        smartBookEntity.bookId = bookId

        for (smartBookPage in smartNotebook.smartBookPages) {
            smartBookPage.bookId = bookId
            val id = smartBookPagesDao.insertSmartBookPage(smartBookPage)
            smartBookPage.id = id
        }
        // TODO: Shouldn't I save atomic notes as well?
        EventBus.getDefault().post(SmartNotebookSaved(smartNotebook, context))

        return smartNotebook
    }

    fun getSmartNotebookContainingNote(noteId: Long): MutableList<SmartNotebook?> {
        val pagesOfNote = smartBookPagesDao.getSmartBookPagesOfNote(noteId)
        if (pagesOfNote.isEmpty()) return ArrayList()

        return pagesOfNote.map { getSmartNotebooks(it.bookId) }
            .filter { it != null }
            .toMutableList()
    }

    val allSmartNotebooks: MutableList<SmartNotebook>
        get() {
            val smartBooks = smartBooksDao.allSmartBooks ?: return ArrayList()
            if (smartBooks.isEmpty()) return ArrayList()

            val smartNotebooks = ArrayList<SmartNotebook>()

            for (smartBook in smartBooks) {
                val smartBookPages = smartBookPagesDao.getSmartBookPages(smartBook.bookId)
                    ?: continue
                if (smartBookPages.isEmpty()) continue

                val noteIds = smartBookPages.map { it.noteId }.toMutableSet()

                val atomicNoteEntities =
                    (atomicNotesDomain.getAtomicNotes(noteIds) ?: continue).filterNotNull().toMutableList()
                smartNotebooks.add(SmartNotebook(smartBook, smartBookPages, atomicNoteEntities))
            }

            return smartNotebooks
        }

    fun getSmartNotebooksForNoteIds(noteIds: MutableSet<Long>): MutableSet<SmartNotebook> {
        val noteIdsLong = noteIds.toList().toMutableSet()
        val smartBookPages = smartBookPagesDao.getSmartBookPagesOfNote(noteIdsLong)
        val bookIdToPageMap =
            ListUtils.groupBy(smartBookPages) { it.bookId }
                .mapValues { it.value.toMutableList() }.toMutableMap()

        val bookIds = bookIdToPageMap.keys.toMutableSet()
        val smartBooks = smartBooksDao.getSmartBooks(bookIds)
        if (smartBooks.isEmpty()) return HashSet()

        val atomicNotes = atomicNotesDomain.getAtomicNotes(noteIdsLong).toList()
        val noteIdToNotes =
            ListUtils.groupBy(atomicNotes) { it.noteId }
                .mapValues { it.value.toMutableList() }.toMutableMap()

        val smartNotebooks = HashSet<SmartNotebook>()
        for (smartBook in smartBooks) {
            val bookPages = bookIdToPageMap[smartBook.bookId] ?: continue
            val bookNotes =
                bookPages.mapNotNull { page -> noteIdToNotes[page.noteId] }.flatten().toMutableList()
            smartNotebooks.add(SmartNotebook(smartBook, bookPages, bookNotes))
        }

        return smartNotebooks
    }

    fun getVirtualSmartNotebooks(bookTitle: String?, noteIds: MutableSet<Long>): SmartNotebook {
        val noteIdsLong = noteIds.toMutableSet()
        val atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIdsLong)
        val smartBookPages = ArrayList<SmartBookPage>()

        var pageIndex = 0
        for (note in atomicNoteEntities) {
            smartBookPages.add(SmartBookPage(-1, note.noteId, pageIndex))
            pageIndex++
        }
        val smartBook = SmartBookEntity(
            -1, bookTitle,
            System.currentTimeMillis(),
            System.currentTimeMillis()
        )
        val notesList = atomicNoteEntities.toMutableList()
        return SmartNotebook(smartBook, smartBookPages, notesList)
    }

    fun getSmartNotebooks(bookId: Long): SmartNotebook? {
        val smartBook = smartBooksDao.getSmartbook(bookId) ?: return null
        val smartBookPages = smartBookPagesDao.getSmartBookPages(bookId)
        if (smartBookPages.isEmpty()) return null

        val noteIds = smartBookPages.map { it.noteId }.toMutableSet()

        val atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIds)
        val smartNotebook = SmartNotebook(smartBook, smartBookPages, atomicNoteEntities)

        return smartNotebook
    }

    fun getSmartNotebooks(title: String): MutableSet<SmartNotebook> {
        if (title.length < 3) return HashSet()

        val smartBooks = smartBooksDao.getSmartbooksWithMatchingTitle("%$title%")
        if (smartBooks.isEmpty()) return HashSet()

        val bookIds = smartBooks.map { it.bookId }.toMutableSet()
        val smartBookPages = smartBookPagesDao.getSmartBooksPages(bookIds)
        val bookIdToPagesMap =
            ListUtils.groupBy(smartBookPages, { it.bookId })
                .mapValues { it.value.toMutableList() }.toMutableMap()

        val noteIds = smartBookPages.map { it.noteId }.toMutableSet()
        val atomicNotes = atomicNotesDomain.getAtomicNotes(noteIds)
        val noteIdToNotes =
            ListUtils.groupBy(atomicNotes,  { it.noteId })
                .mapValues { it.value.toMutableList() }.toMutableMap()

        val smartNotebooks = mutableSetOf<SmartNotebook>()
        for (smartBook in smartBooks) {
            val bookPages = bookIdToPagesMap[smartBook.bookId] ?: continue
            val bookNotes = bookPages.mapNotNull { page -> noteIdToNotes[page.noteId] }.flatten().toMutableList()
            smartNotebooks.add(SmartNotebook(smartBook, bookPages, bookNotes))
        }

        return smartNotebooks
    }

    private fun newSmartBook(title: String?, createdDateTimeMs: Long): SmartBookEntity {
        val smartBookEntity = SmartBookEntity()
        if (!isNullOrWhitespace(title)) {
            smartBookEntity.title = title
        }
        smartBookEntity.createdTimeMillis = createdDateTimeMs
        smartBookEntity.lastModifiedTimeMillis = createdDateTimeMs

        val bookId = smartBooksDao.insertSmartBook(smartBookEntity)
        smartBookEntity.bookId = bookId

        return smartBookEntity
    }

    fun newSmartBookPage(
        smartBookEntity: SmartBookEntity,
        atomicNoteEntity: AtomicNoteEntity,
        pageOrder: Int
    ): SmartBookPage {
        val smartBookPage = SmartBookPage(
            smartBookEntity.bookId,
            atomicNoteEntity.noteId,
            pageOrder
        )
        val id = smartBookPagesDao.insertSmartBookPage(smartBookPage)
        smartBookPage.id = id

        return smartBookPage
    }


    fun bookExists(notebook: SmartNotebook): Boolean {
        val smartBook = notebook.smartBook ?: return false
        val bookInDb = smartBooksDao.getSmartbook(smartBook.bookId)
        return bookInDb != null
    }

    val allSmartBookPages: MutableList<SmartBookPage>
        get() = smartBookPagesDao.allSmartBookPages
}



























