package org.basnalcorp.shared.data.repository

import org.basnalcorp.shared.domain.SmartNotebook

/**
 * High-level notebook operations (aggregate of book, pages, atomic notes).
 * File system operations (create/delete notebook directory) are platform-specific
 * and should be done by the app when using this repository.
 */
interface SmartNotebookRepository {
    fun getAll(): List<SmartNotebook>
    fun getByBookId(bookId: Long): SmartNotebook?
    fun getByTitlePattern(pattern: String): Set<SmartNotebook>
    /** Persists a new book + first page + atomic note; returns the saved aggregate with generated ids. */
    fun createNotebook(title: String, directoryPath: String, noteType: String): SmartNotebook
    fun updateNotebook(notebook: SmartNotebook)
    fun deleteNotebook(notebook: SmartNotebook)
}
