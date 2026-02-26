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
    /** Appends a new atomic note and page to the notebook; returns the updated notebook. */
    fun addPage(notebook: SmartNotebook): SmartNotebook
    /** Appends a new note with the given type (e.g. "text_note", "handwritten_png"); returns the updated notebook. */
    fun addPage(notebook: SmartNotebook, noteType: String): SmartNotebook
    fun updateNotebook(notebook: SmartNotebook)
    fun deleteNotebook(notebook: SmartNotebook)
}
