package com.originb.inkwisenote2.modules.repositories

import android.app.Application
import android.content.Context
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.ListUtils
import com.originb.inkwisenote2.common.Strings.isNullOrWhitespace
import com.originb.inkwisenote2.modules.backgroundjobs.Events.*
import com.originb.inkwisenote2.modules.smartnotes.data.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

class SmartNotebookRepository(
    private val atomicNotesDomain: AtomicNotesDomain,
    private val atomicNoteEntitiesDao: AtomicNoteEntitiesDao,
    private val smartBooksDao: SmartBooksDao,
    private val smartBookPagesDao: SmartBookPagesDao
) {
    // Create the data and return the notebook entity
    fun initializeNewSmartNotebook(
        title: String?,
        directoryPath: String?,
        noteType: NoteType?
    ): Optional<SmartNotebook?> {
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
        return Optional.ofNullable<SmartNotebook?>(smartNotebook)
    }

    fun deleteSmartNotebook(smartNotebook: SmartNotebook) {
        // Delete notebook folder from filesystem if it exists
        try {
            if (!smartNotebook.getAtomicNotes().isEmpty()) {
                val firstNote = smartNotebook.getAtomicNotes().get(0)
                if (firstNote != null && firstNote.getFilepath() != null) {
                    val notebookDir = File(firstNote.getFilepath())
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
        smartBookPagesDao.deleteSmartBookPages(smartNotebook.getSmartBook().getBookId())

        smartNotebook.getAtomicNotes()
            .stream().map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }
            .forEach { noteId: Long? -> atomicNoteEntitiesDao.deleteAtomicNote(noteId) }

        smartBooksDao.deleteSmartBook(smartNotebook.getSmartBook().getBookId())

        EventBus.getDefault().post(NotebookDeleted(smartNotebook))
    }

    fun deleteNoteFromBook(smartNotebook: SmartNotebook, atomicNote: AtomicNoteEntity) {
        // will pages allow this to be deleted first?
        if (smartNotebook.getAtomicNotes().size <= 1) deleteSmartNotebook(smartNotebook)
        else {
            smartBookPagesDao.deleteNotePages(atomicNote.noteId)
            atomicNoteEntitiesDao.deleteAtomicNote(atomicNote.noteId)

            EventBus.getDefault().post(NoteDeleted(smartNotebook, atomicNote))
        }
    }

    fun updateNotebook(smartNotebook: SmartNotebook, context: Context?) {
        val updateTime = System.currentTimeMillis()

        val smartBookEntity = smartNotebook.getSmartBook()

        // means this is a virtual notebook
        if (smartBookEntity.getBookId() == -1L) {
            return
        }

        smartBookEntity.setLastModifiedTimeMillis(System.currentTimeMillis())
        smartBooksDao.updateSmartBook(smartBookEntity)

        for (atomicNote in smartNotebook.getAtomicNotes()) {
            atomicNote.setLastModifiedTimeMillis(updateTime)
        }
        atomicNotesDomain.updateAtomicNotes(smartNotebook.getAtomicNotes())

        val updateResult = smartBookPagesDao.updateSmartBookPage(smartNotebook.getSmartBookPages())
        EventBus.getDefault().post(SmartNotebookSaved(smartNotebook, context))
    }

    fun saveSmartNotebook(smartNotebook: SmartNotebook, context: Application?): SmartNotebook {
        val updateTime = System.currentTimeMillis()
        val smartBookEntity = smartNotebook.getSmartBook()
        // means this is not a new notebook
        if (smartBookEntity.getBookId() > -1) {
            return smartNotebook
        }

        smartBookEntity.setCreatedTimeMillis(updateTime)
        smartBookEntity.setLastModifiedTimeMillis(updateTime)

        val bookId = smartBooksDao.insertSmartBook(smartBookEntity)
        smartBookEntity.setBookId(bookId)

        for (smartBookPage in smartNotebook.getSmartBookPages()) {
            smartBookPage.setBookId(bookId)
            val id = smartBookPagesDao.insertSmartBookPage(smartBookPage)
            smartBookPage.setId(id)
        }
        // TODO: Shouldn't I save atomic notes as well?
        EventBus.getDefault().post(SmartNotebookSaved(smartNotebook, context))

        return smartNotebook
    }

    fun getSmartNotebookContainingNote(noteId: Long): MutableList<SmartNotebook?> {
        val pagesOfNote = smartBookPagesDao.getSmartBookPagesOfNote(noteId)
        if (pagesOfNote == null || pagesOfNote.isEmpty()) return ArrayList<SmartNotebook?>()

        val smartNotebooks = pagesOfNote.stream().map<Long?> { obj: Function<in R?, out V?>? -> obj.getBookId() }
            .map<Optional<SmartNotebook?>?> { bookId: Function<in R?, out V?>? -> this.getSmartNotebooks(bookId) }
            .filter { obj: Predicate<in T?>? -> obj.isPresent() }
            .map<SmartNotebook?> { obj: Function<in R?, out V?>? -> obj.get() }
            .collect(Collectors.toList())

        return smartNotebooks
    }

    val allSmartNotebooks: MutableList<SmartNotebook?>
        get() {
            val smartBooks = smartBooksDao.allSmartBooks
            if (smartBooks == null || smartBooks.isEmpty()) return ArrayList<SmartNotebook?>()

            val smartNotebooks: MutableList<SmartNotebook?> = ArrayList<SmartNotebook?>()

            for (smartBook in smartBooks) {
                val smartBookPages =
                    smartBookPagesDao.getSmartBookPages(smartBook.bookId)
                if (smartBookPages == null || smartBookPages.isEmpty()) continue

                val noteIds = smartBookPages.stream()
                    .map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }
                    .collect(Collectors.toSet())

                val atomicNoteEntities =
                    atomicNotesDomain.getAtomicNotes(noteIds)

                smartNotebooks.add(SmartNotebook(smartBook, smartBookPages, atomicNoteEntities))
            }

            return smartNotebooks
        }

    fun getSmartNotebooksForNoteIds(noteIds: MutableSet<Long?>?): MutableSet<SmartNotebook?> {
        val smartBookPages = smartBookPagesDao.getSmartBookPagesOfNote(noteIds)
        val bookIdToPageMap: MutableMap<Long?, MutableList<SmartBookPage?>?> = ListUtils.groupBy<SmartBookPage?, Long?>(
            smartBookPages,
            Function { obj: Function<in R?, out V?>? -> obj.getBookId() })

        val smartBooks = smartBooksDao.getSmartBooks(bookIdToPageMap.keys)
        if (smartBooks == null || smartBooks.isEmpty()) return HashSet<SmartNotebook?>()

        val atomicNotes = atomicNotesDomain.getAtomicNotes(noteIds)
        val noteIdToNotes: MutableMap<Long?, MutableList<AtomicNoteEntity?>?> =
            ListUtils.groupBy<AtomicNoteEntity?, Long?>(
                atomicNotes,
                Function { obj: Function<in R?, out V?>? -> obj.getNoteId() })

        val bookIdsToRemove: MutableSet<Long?> = HashSet<Long?>()
        val smartNotebooks: MutableSet<SmartNotebook?> = HashSet<SmartNotebook?>()
        for (smartBook in smartBooks) {
            if (!bookIdToPageMap.containsKey(smartBook.bookId)) {
                bookIdsToRemove.add(smartBook.bookId)
                continue
            }
            val bookPages = bookIdToPageMap.get(smartBook.bookId)
            val bookNotes = bookPages!!.stream().map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }
                .map<MutableList<AtomicNoteEntity?>?> { key: Function<in R?, out V?>? -> noteIdToNotes.get(key) }
                .filter { obj: Predicate<in T?>? -> Objects.nonNull(obj) }
                .flatMap<AtomicNoteEntity?> { obj: Function<in R?, out V?>? -> obj.stream() }
                .collect(Collectors.toList())

            smartNotebooks.add(SmartNotebook(smartBook, smartBookPages, bookNotes))
        }

        return smartNotebooks
    }

    fun getVirtualSmartNotebooks(bookTitle: String?, noteIds: MutableSet<Long?>?): Optional<SmartNotebook?> {
        val atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIds)
        val smartBookPages: MutableList<SmartBookPage> = ArrayList<SmartBookPage>()

        var pageIndex = 0
        for (note in atomicNoteEntities) {
            smartBookPages.add(
                SmartBookPage(
                    -1,
                    note.noteId,
                    pageIndex
                )
            )
            pageIndex++
        }
        val smartBook = SmartBookEntity(
            -1, bookTitle,
            System.currentTimeMillis(),
            System.currentTimeMillis()
        )

        return Optional.of<SmartNotebook?>(SmartNotebook(smartBook, smartBookPages, atomicNoteEntities))
    }

    fun getSmartNotebooks(bookId: Long): Optional<SmartNotebook?> {
        val smartBook = smartBooksDao.getSmartbook(bookId)
        if (smartBook == null) return Optional.empty<SmartNotebook?>()

        val smartBookPages = smartBookPagesDao.getSmartBookPages(bookId)
        if (smartBookPages == null || smartBookPages.isEmpty()) return Optional.empty<SmartNotebook?>()

        val noteIds = smartBookPages.stream()
            .map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }
            .collect(Collectors.toSet())

        val atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIds)

        val smartNotebook = SmartNotebook(smartBook, smartBookPages, atomicNoteEntities)

        return Optional.ofNullable<SmartNotebook?>(smartNotebook)
    }

    fun getSmartNotebooks(title: String): MutableSet<SmartNotebook?> {
        if (title.length < 3) return HashSet<SmartNotebook?>()

        val smartBooks = smartBooksDao.getSmartbooksWithMatchingTitle("%" + title + "%")
        if (smartBooks == null || smartBooks.isEmpty()) return HashSet<SmartNotebook?>()

        val bookIds = smartBooks.stream().map<Long?> { obj: Function<in R?, out V?>? -> obj.getBookId() }.collect(
            Collectors.toSet()
        )
        val smartBookPages = smartBookPagesDao.getSmartBooksPages(bookIds)
        val bookIdToPagesMap: MutableMap<Long?, MutableList<SmartBookPage>?> = ListUtils.groupBy<SmartBookPage?, Long?>(
            smartBookPages,
            Function { obj: Function<in R?, out V?>? -> obj.getBookId() })

        val noteIds = smartBookPages.stream().map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }.collect(
            Collectors.toSet()
        )
        val atomicNotes = atomicNotesDomain.getAtomicNotes(noteIds)
        val noteIdToNotes: MutableMap<Long?, MutableList<AtomicNoteEntity?>?> =
            ListUtils.groupBy<AtomicNoteEntity?, Long?>(
                atomicNotes,
                Function { obj: Function<in R?, out V?>? -> obj.getNoteId() })

        val smartNotebooks: MutableSet<SmartNotebook?> = HashSet<SmartNotebook?>()
        val bookIdsOfEmptyBooks: MutableSet<Long?> = HashSet<Long?>()
        for (smartBook in smartBooks) {
            val bookId = smartBook.bookId
            if (!bookIdToPagesMap.containsKey(bookId)) {
                bookIdsOfEmptyBooks.add(bookId)
                continue
            }
            val bookPages = bookIdToPagesMap.get(bookId)
            val bookNotes = bookPages!!.stream().map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }
                .map<MutableList<AtomicNoteEntity?>?> { key: Function<in R?, out V?>? -> noteIdToNotes.get(key) }
                .filter { obj: Predicate<in T?>? -> Objects.nonNull(obj) }
                .flatMap<AtomicNoteEntity?> { obj: Function<in R?, out V?>? -> obj.stream() }
                .collect(Collectors.toList())
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
        val smartBook = notebook.getSmartBook()
        if (smartBook == null) return false
        val bookId = smartBook.getBookId()
        val bookInDb = smartBooksDao.getSmartbook(bookId)
        return bookInDb != null
    }

    val allSmartBookPages: MutableList<SmartBookPage?>?
        get() = smartBookPagesDao.allSmartBookPages
}



























