package org.basnalcorp.shared.data.repository.impl

import org.basnalcorp.shared.data.repository.SmartBooksRepository
import org.basnalcorp.shared.db.NotesDatabase
import org.basnalcorp.shared.domain.SmartBook

class SmartBooksRepositoryImpl(private val db: NotesDatabase) : SmartBooksRepository {

    override fun insert(smartBook: SmartBook): Long {
        db.smartBooksQueries.insertSmartBook(
            title = smartBook.title,
            created_time_ms = smartBook.createdTimeMillis,
            last_modified_time_ms = smartBook.lastModifiedTimeMillis
        )
        return db.lastInsertRowIdQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(smartBook: SmartBook) {
        db.smartBooksQueries.updateSmartBook(
            title = smartBook.title,
            created_time_ms = smartBook.createdTimeMillis,
            last_modified_time_ms = smartBook.lastModifiedTimeMillis,
            book_id = smartBook.bookId
        )
    }

    override fun delete(bookId: Long) {
        db.smartBooksQueries.deleteSmartBook(book_id = bookId)
    }

    override fun get(bookId: Long): SmartBook? =
        db.smartBooksQueries.getSmartBook(book_id = bookId).executeAsOneOrNull()?.toDomain()

    override fun getByIds(bookIds: Set<Long>): List<SmartBook> {
        if (bookIds.isEmpty()) return emptyList()
        return db.smartBooksQueries.getSmartBooksByIds(bookIds).executeAsList().map { it.toDomain() }
    }

    override fun getAll(): List<SmartBook> =
        db.smartBooksQueries.allSmartBooks().executeAsList().map { it.toDomain() }

    override fun getByTitlePattern(pattern: String): List<SmartBook> =
        db.smartBooksQueries.getSmartBooksByTitle(pattern).executeAsList().map { it.toDomain() }
}

private fun org.basnalcorp.shared.db.Smart_books.toDomain() = SmartBook(
    bookId = book_id,
    title = title,
    createdTimeMillis = created_time_ms,
    lastModifiedTimeMillis = last_modified_time_ms
)
