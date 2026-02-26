package org.basnalcorp.shared.data.repository.impl

import org.basnalcorp.shared.data.repository.SmartBookPagesRepository
import org.basnalcorp.shared.db.NotesDatabase
import org.basnalcorp.shared.domain.SmartBookPage

class SmartBookPagesRepositoryImpl(private val db: NotesDatabase) : SmartBookPagesRepository {

    override fun insert(bookId: Long, noteId: Long, pageOrder: Int): Long {
        db.smartBookPagesQueries.insertSmartBookPage(
            book_id = bookId,
            note_id = noteId,
            page_order = pageOrder.toLong()
        )
        return db.lastInsertRowIdQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(page: SmartBookPage) {
        db.smartBookPagesQueries.updateSmartBookPage(
            book_id = page.bookId,
            note_id = page.noteId,
            page_order = page.pageOrder,
            id = page.id
        )
    }

    override fun deleteByBookId(bookId: Long) {
        db.smartBookPagesQueries.deleteSmartBookPagesByBookId(book_id = bookId)
    }

    override fun deleteByNoteId(noteId: Long) {
        db.smartBookPagesQueries.deleteNotePages(note_id = noteId)
    }

    override fun getPagesForBook(bookId: Long): List<SmartBookPage> =
        db.smartBookPagesQueries.getSmartBookPages(book_id = bookId).executeAsList().map { it.toDomain() }

    override fun getPagesForNotes(noteIds: Set<Long>): List<SmartBookPage> {
        if (noteIds.isEmpty()) return emptyList()
        return db.smartBookPagesQueries.getSmartBookPagesOfNote(noteIds).executeAsList().map { it.toDomain() }
    }
}

private fun org.basnalcorp.shared.db.Smart_book_pages.toDomain() = SmartBookPage(
    id = id,
    bookId = book_id,
    noteId = note_id,
    pageOrder = page_order
)
