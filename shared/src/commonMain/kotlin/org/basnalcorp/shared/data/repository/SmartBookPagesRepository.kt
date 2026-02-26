package org.basnalcorp.shared.data.repository

import org.basnalcorp.shared.domain.SmartBookPage

interface SmartBookPagesRepository {
    fun insert(bookId: Long, noteId: Long, pageOrder: Int): Long
    fun update(page: SmartBookPage)
    fun deleteByBookId(bookId: Long)
    fun deleteByNoteId(noteId: Long)
    fun getPagesForBook(bookId: Long): List<SmartBookPage>
    fun getPagesForNotes(noteIds: Set<Long>): List<SmartBookPage>
}
