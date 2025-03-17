package com.originb.inkwisenote2.modules.repositories

import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.modules.smartnotes.data.*
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

class SmartNotebookRepository {
    private val atomicNoteEntitiesDao: AtomicNoteEntitiesDao =
        Repositories.Companion.getInstance().getNotesDb().atomicNoteEntitiesDao()
    private val smartBooksDao: SmartBooksDao =
        Repositories.Companion.getInstance().getNotesDb().smartBooksDao()
    private val smartBookPagesDao: SmartBookPagesDao =
        Repositories.Companion.getInstance().getNotesDb().smartBookPagesDao()

    // Create the data and return the notebook entity
    fun initializeNewSmartNotebook(
        title: String,
        directoryPath: String?,
        noteType: NoteType
    ): Optional<SmartNotebook> {
        val atomicNoteEntity = newAtomicNote("", directoryPath, noteType)
        val smartBookEntity = newSmartBook(title, atomicNoteEntity.createdTimeMillis)
        val smartBookPage = newSmartBookPage(smartBookEntity, atomicNoteEntity, 0)
        val smartNotebook = SmartNotebook(smartBookEntity, smartBookPage, atomicNoteEntity)
        return Optional.ofNullable(smartNotebook)
    }

    fun deleteSmartNotebook(smartNotebook: SmartNotebook?) {
        // will pages allow this to be deleted first?
        smartNotebook.getAtomicNotes()
            .stream().map { obj: AtomicNoteEntity -> obj.noteId }
            .forEach { noteId: Long? -> atomicNoteEntitiesDao.deleteAtomicNote(noteId) }

        smartBookPagesDao.deleteSmartBookPages(smartNotebook.getSmartBook().bookId)

        smartBooksDao.deleteSmartBook(smartNotebook.getSmartBook().bookId)
    }

    fun deleteNoteFromBook(smartNotebook: SmartNotebook?, atomicNote: AtomicNoteEntity?) {
        // will pages allow this to be deleted first?
        atomicNoteEntitiesDao.deleteAtomicNote(atomicNote.getNoteId())
        smartBookPagesDao.deleteNotePages(atomicNote.getNoteId())

        getSmartNotebooks(smartNotebook.getSmartBook().bookId)
            .filter(Predicate { updatedSmartNotebook: SmartNotebook -> updatedSmartNotebook.atomicNotes!!.isEmpty() })
            .ifPresent { updatedSmartNotebook: SmartNotebook? ->
                smartBookPagesDao.deleteSmartBookPages(smartNotebook!!.smartBook.bookId)
                smartBooksDao.deleteSmartBook(smartNotebook.getSmartBook().bookId)
            }
    }

    fun updateNotebook(smartNotebook: SmartNotebook?) {
        val updateTime = System.currentTimeMillis()

        val smartBookEntity = smartNotebook.getSmartBook()
        smartBookEntity.lastModifiedTimeMillis = System.currentTimeMillis()
        smartBooksDao.updateSmartBook(smartBookEntity)

        for (atomicNote in smartNotebook.getAtomicNotes()) {
            atomicNote.lastModifiedTimeMillis = updateTime
        }
        atomicNoteEntitiesDao.updateAtomicNote(smartNotebook.getAtomicNotes())

        val updateResult = smartBookPagesDao.updateSmartBookPage(smartNotebook.getSmartBookPages())
    }

    fun getSmartNotebookContainingNote(noteId: Long): Optional<SmartNotebook?> {
        val pagesOfNote = smartBookPagesDao.getSmartBookPagesOfNote(noteId)
        if (pagesOfNote == null || pagesOfNote.isEmpty()) return Optional.empty()

        // TODO: only get the first page for now. We will fetch more later
        val bookId = pagesOfNote.stream().findFirst().map { obj: SmartBookPage? -> obj.getBookId() }.get()

        return getSmartNotebooks(bookId)
    }

    val allSmartNotebooks: List<SmartNotebook>
        get() {
            val smartBooks = smartBooksDao.allSmartBooks
            if (smartBooks == null || smartBooks.isEmpty()) return ArrayList()

            val smartNotebooks: MutableList<SmartNotebook> = ArrayList()

            for (smartBook in smartBooks) {
                val smartBookPages = smartBookPagesDao.getSmartBookPages(smartBook.bookId)
                if (smartBookPages == null || smartBookPages.isEmpty()) continue

                val noteIds = smartBookPages.stream()
                    .map { obj: SmartBookPage? -> obj.getNoteId() }
                    .collect(Collectors.toSet())

                val atomicNoteEntities = atomicNoteEntitiesDao.getAtomicNotes(noteIds)

                smartNotebooks.add(SmartNotebook(smartBook, smartBookPages, atomicNoteEntities))
            }

            return smartNotebooks
        }

    fun getSmartNotebooksForNoteIds(noteIds: Set<Long>?): Set<SmartNotebook> {
        val smartBookPages = smartBookPagesDao.getSmartBookPagesOfNote(noteIds)
        val bookIdToPageMap: Map<Long?, MutableList<SmartBookPage?>?> =
            ListUtils.groupBy(smartBookPages, Function { obj: SmartBookPage? -> obj.getBookId() })!!

        val smartBooks = smartBooksDao.getSmartBooks(bookIdToPageMap.keys)
        if (smartBooks == null || smartBooks.isEmpty()) return HashSet()

        val atomicNotes = atomicNoteEntitiesDao.getAtomicNotes(noteIds)
        val noteIdToNotes: Map<Long?, MutableList<AtomicNoteEntity?>?> =
            ListUtils.groupBy(atomicNotes, Function { obj: AtomicNoteEntity? -> obj.getNoteId() })!!

        val bookIdsToRemove: MutableSet<Long> = HashSet()
        val smartNotebooks: MutableSet<SmartNotebook> = HashSet()
        for (smartBook in smartBooks) {
            if (!bookIdToPageMap.containsKey(smartBook.bookId)) {
                bookIdsToRemove.add(smartBook.bookId)
                continue
            }
            val bookPages: List<SmartBookPage?>? = bookIdToPageMap[smartBook.bookId]
            val bookNotes = bookPages!!.stream().map { obj: SmartBookPage? -> obj.getNoteId() }
                .map<List<AtomicNoteEntity?>?> { key: Long? -> noteIdToNotes[key] }
                .filter { obj: List<AtomicNoteEntity?>? -> Objects.nonNull(obj) }
                .flatMap { obj: List<AtomicNoteEntity?>? -> obj!!.stream() }
                .collect(Collectors.toList())

            smartNotebooks.add(SmartNotebook(smartBook, smartBookPages, bookNotes))
        }

        return smartNotebooks
    }

    fun getSmartNotebooks(bookId: Long): Optional<SmartNotebook?> {
        val smartBook = smartBooksDao.getSmartbooksWithMatchingTitle(bookId)
            ?: return Optional.empty()

        val smartBookPages = smartBookPagesDao.getSmartBookPages(bookId)
        if (smartBookPages == null || smartBookPages.isEmpty()) return Optional.empty()

        val noteIds = smartBookPages.stream()
            .map { obj: SmartBookPage? -> obj.getNoteId() }
            .collect(Collectors.toSet())

        val atomicNoteEntities = atomicNoteEntitiesDao.getAtomicNotes(noteIds)

        val smartNotebook = SmartNotebook(smartBook, smartBookPages, atomicNoteEntities)

        return Optional.ofNullable(smartNotebook)
    }

    fun getSmartNotebooks(title: String): MutableSet<SmartNotebook> {
        if (title.length < 3) return HashSet()

        val smartBooks = smartBooksDao.getSmartbooksWithMatchingTitle("%$title%")
        if (smartBooks == null || smartBooks.isEmpty()) return HashSet()

        val bookIds = smartBooks.stream().map { obj: SmartBookEntity? -> obj.getBookId() }.collect(Collectors.toSet())
        val smartBookPages = smartBookPagesDao.getSmartBooksPages(bookIds)
        val bookIdToPagesMap: Map<Long?, MutableList<SmartBookPage?>?> =
            ListUtils.groupBy(smartBookPages, Function { obj: SmartBookPage? -> obj.getBookId() })!!

        val noteIds =
            smartBookPages!!.stream().map { obj: SmartBookPage? -> obj.getNoteId() }.collect(Collectors.toSet())
        val atomicNotes = atomicNoteEntitiesDao.getAtomicNotes(noteIds)
        val noteIdToNotes: Map<Long?, MutableList<AtomicNoteEntity?>?> =
            ListUtils.groupBy(atomicNotes, Function { obj: AtomicNoteEntity? -> obj.getNoteId() })!!

        val smartNotebooks: MutableSet<SmartNotebook> = HashSet()
        val bookIdsOfEmptyBooks: MutableSet<Long> = HashSet()
        for (smartBook in smartBooks) {
            val bookId = smartBook.bookId
            if (!bookIdToPagesMap.containsKey(bookId)) {
                bookIdsOfEmptyBooks.add(bookId)
                continue
            }
            val bookPages = bookIdToPagesMap[bookId]
            val bookNotes = bookPages!!.stream().map { obj: SmartBookPage? -> obj.getNoteId() }
                .map<List<AtomicNoteEntity?>?> { key: Long? -> noteIdToNotes[key] }
                .filter { obj: List<AtomicNoteEntity?>? -> Objects.nonNull(obj) }
                .flatMap { obj: List<AtomicNoteEntity?>? -> obj!!.stream() }
                .collect(Collectors.toList())
            smartNotebooks.add(SmartNotebook(smartBook, bookPages, bookNotes))
        }

        return smartNotebooks
    }

    fun newAtomicNote(filename: String?, filepath: String?, noteType: NoteType): AtomicNoteEntity {
        val createdTimeMillis = System.currentTimeMillis()
        val atomicNoteEntity = AtomicNoteEntity()
        atomicNoteEntity.createdTimeMillis = createdTimeMillis

        if (Strings.isNullOrWhitespace(filename)) {
            atomicNoteEntity.filename = createdTimeMillis.toString()
        } else {
            atomicNoteEntity.filename = filename
        }
        if (Strings.isNullOrWhitespace(filepath)) {
            atomicNoteEntity.filepath = ""
        } else {
            atomicNoteEntity.filepath = filepath
        }

        atomicNoteEntity.noteType = noteType.toString()

        val noteId = atomicNoteEntitiesDao.insertAtomicNote(atomicNoteEntity)
        atomicNoteEntity.noteId = noteId

        return atomicNoteEntity
    }

    private fun newSmartBook(title: String, createdDateTimeMs: Long): SmartBookEntity {
        val smartBookEntity = SmartBookEntity()
        if (Strings.isNullOrWhitespace(title)) {
            smartBookEntity.title = DateTimeUtils.msToDateTime(createdDateTimeMs)
        } else {
            smartBookEntity.title = title
        }
        smartBookEntity.createdTimeMillis = createdDateTimeMs
        smartBookEntity.lastModifiedTimeMillis = createdDateTimeMs

        val bookId = smartBooksDao.insertSmartBook(smartBookEntity)
        smartBookEntity.bookId = bookId

        return smartBookEntity
    }

    fun newSmartBookPage(
        smartBookEntity: SmartBookEntity?,
        atomicNoteEntity: AtomicNoteEntity?,
        pageOrder: Int
    ): SmartBookPage {
        val smartBookPage = SmartBookPage(
            smartBookEntity.getBookId(),
            atomicNoteEntity.getNoteId(),
            pageOrder
        )
        val id = smartBookPagesDao.insertSmartBookPage(smartBookPage)
        smartBookPage.id = id

        return smartBookPage
    }
}



























