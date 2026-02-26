package org.basnalcorp.shared.data.repository.impl

import org.basnalcorp.shared.data.repository.AtomicNotesRepository
import org.basnalcorp.shared.data.repository.SmartBookPagesRepository
import org.basnalcorp.shared.data.repository.SmartBooksRepository
import org.basnalcorp.shared.data.repository.SmartNotebookRepository
import org.basnalcorp.shared.data.repository.TextNotesRepository
import org.basnalcorp.shared.domain.AtomicNote
import org.basnalcorp.shared.domain.SmartBook
import org.basnalcorp.shared.domain.SmartBookPage
import org.basnalcorp.shared.domain.SmartNotebook
import org.basnalcorp.shared.domain.TextNote
import org.basnalcorp.shared.util.isNullOrWhitespace
import org.basnalcorp.shared.createNotebookDirectory
import org.basnalcorp.shared.writeTextFile
import kotlinx.datetime.Clock

class SmartNotebookRepositoryImpl(
    private val atomicNotes: AtomicNotesRepository,
    private val smartBooks: SmartBooksRepository,
    private val smartBookPages: SmartBookPagesRepository,
    private val textNotesRepository: TextNotesRepository
) : SmartNotebookRepository {

    override fun getAll(): List<SmartNotebook> {
        val books = smartBooks.getAll()
        return books.mapNotNull { getByBookId(it.bookId) }
    }

    override fun getByBookId(bookId: Long): SmartNotebook? {
        val book = smartBooks.get(bookId) ?: return null
        val pages = smartBookPages.getPagesForBook(bookId)
        if (pages.isEmpty()) return null
        val noteIds = pages.map { it.noteId }.toSet()
        val notes = atomicNotes.getByIds(noteIds)
        if (notes.size != noteIds.size) return null
        val noteMap = notes.associateBy { it.noteId }
        val orderedNotes = pages.mapNotNull { noteMap[it.noteId] }
        return SmartNotebook(
            smartBook = book,
            smartBookPages = pages.toMutableList(),
            atomicNotes = orderedNotes.toMutableList()
        )
    }

    override fun getByTitlePattern(pattern: String): Set<SmartNotebook> {
        if (isNullOrWhitespace(pattern) || pattern.length < 3) return emptySet()
        val books = smartBooks.getByTitlePattern("%$pattern%")
        return books.mapNotNull { getByBookId(it.bookId) }.toSet()
    }

    override fun createNotebook(title: String, directoryPath: String, noteType: String): SmartNotebook {
        val now = Clock.System.now().toEpochMilliseconds()
        val safeTitle = title.trim().ifBlank { now.toString() }
        val notebookDir = createNotebookDirectory(directoryPath, safeTitle)
        val filename = now.toString()
        val note = AtomicNote(
            filename = filename,
            filepath = notebookDir,
            noteType = noteType,
            createdTimeMillis = now,
            lastModifiedTimeMillis = now
        )
        val noteId = atomicNotes.insert(note)
        note.noteId = noteId
        val book = SmartBook(
            title = safeTitle,
            createdTimeMillis = now,
            lastModifiedTimeMillis = now
        )
        val bookId = smartBooks.insert(book)
        book.bookId = bookId
        if (noteType == "text_note") {
            textNotesRepository.insertOrReplace(
                TextNote(
                    noteId = noteId,
                    bookId = bookId,
                    noteText = "",
                    createdTimeMillis = now,
                    lastModifiedTimeMillis = now
                )
            )
            try {
                writeTextFile("$notebookDir/$filename.md", "")
            } catch (_: Exception) { }
        }
        val pageId = smartBookPages.insert(bookId, noteId, 0)
        val page = SmartBookPage(id = pageId, bookId = bookId, noteId = noteId, pageOrder = 0)
        return SmartNotebook(
            smartBook = book,
            smartBookPages = mutableListOf(page),
            atomicNotes = mutableListOf(note)
        )
    }

    override fun addPage(notebook: SmartNotebook): SmartNotebook {
        val directoryPath = notebook.atomicNotes.firstOrNull()?.filepath ?: return notebook
        val now = Clock.System.now().toEpochMilliseconds()
        val newNote = AtomicNote(
            filename = now.toString(),
            filepath = directoryPath,
            noteType = "not_set",
            createdTimeMillis = now,
            lastModifiedTimeMillis = now
        )
        val noteId = atomicNotes.insert(newNote)
        newNote.noteId = noteId
        val bookId = notebook.smartBook.bookId
        val pageOrder = notebook.smartBookPages.size
        val pageId = smartBookPages.insert(bookId, noteId, pageOrder)
        val newPage = SmartBookPage(id = pageId, bookId = bookId, noteId = noteId, pageOrder = pageOrder.toLong())
        notebook.smartBookPages.add(newPage)
        notebook.atomicNotes.add(newNote)
        notebook.smartBook.lastModifiedTimeMillis = now
        smartBooks.update(notebook.smartBook)
        return notebook
    }

    override fun addPage(notebook: SmartNotebook, noteType: String): SmartNotebook {
        val directoryPath = notebook.atomicNotes.firstOrNull()?.filepath ?: return notebook
        val now = Clock.System.now().toEpochMilliseconds()
        val filename = now.toString()
        val newNote = AtomicNote(
            filename = filename,
            filepath = directoryPath,
            noteType = noteType,
            createdTimeMillis = now,
            lastModifiedTimeMillis = now
        )
        val noteId = atomicNotes.insert(newNote)
        newNote.noteId = noteId
        val bookId = notebook.smartBook.bookId
        if (noteType == "text_note") {
            textNotesRepository.insertOrReplace(
                TextNote(
                    noteId = noteId,
                    bookId = bookId,
                    noteText = "",
                    createdTimeMillis = now,
                    lastModifiedTimeMillis = now
                )
            )
            try {
                writeTextFile("$directoryPath/$filename.md", "")
            } catch (_: Exception) { }
        }
        val pageOrder = notebook.smartBookPages.size
        val pageId = smartBookPages.insert(bookId, noteId, pageOrder)
        val newPage = SmartBookPage(id = pageId, bookId = bookId, noteId = noteId, pageOrder = pageOrder.toLong())
        notebook.smartBookPages.add(newPage)
        notebook.atomicNotes.add(newNote)
        notebook.smartBook.lastModifiedTimeMillis = now
        smartBooks.update(notebook.smartBook)
        return notebook
    }

    override fun updateNotebook(notebook: SmartNotebook) {
        notebook.smartBook.lastModifiedTimeMillis = Clock.System.now().toEpochMilliseconds()
        smartBooks.update(notebook.smartBook)
        notebook.atomicNotes.forEach { atomicNotes.update(it) }
        notebook.smartBookPages.forEach { smartBookPages.update(it) }
    }

    override fun deleteNotebook(notebook: SmartNotebook) {
        notebook.smartBook.bookId.let { smartBookPages.deleteByBookId(it) }
        notebook.atomicNotes.forEach { atomicNotes.delete(it.noteId) }
        smartBooks.delete(notebook.smartBook.bookId)
    }
}
