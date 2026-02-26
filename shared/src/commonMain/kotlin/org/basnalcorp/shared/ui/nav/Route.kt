package org.basnalcorp.shared.ui.nav

/**
 * Root navigation routes (Phase 7.2).
 * Host maintains back stack; shared UI receives current route and onNavigate/onBack.
 */
sealed class Route {
    /** Notebook list (home). */
    data object Home : Route()

    /** Search / all notebooks. */
    data object Search : Route()

    /** Saved queries list. */
    data object QueryList : Route()

    /** Query results for a saved query. */
    data class QueryResults(val queryName: String) : Route()

    /** Create/edit query. */
    data object QueryCreation : Route()

    /** Init new note: notebook name + note type (text/handwritten) before creating. */
    data class InitNote(val workingPath: String) : Route()

    /** Smart notebook detail (book with pages). */
    data class SmartNotebook(
        val bookId: Long? = null,
        val workingPath: String? = null,
        val bookTitle: String? = null,
        val noteIds: String? = null,
        val selectedNoteId: Long? = null
    ) : Route()

    /** Note detail (text or handwritten). */
    data class NoteDetail(
        val bookId: Long,
        val noteId: Long,
        val isHandwritten: Boolean
    ) : Route()

    /** Admin / debug. */
    data object Admin : Route()

    /** File explorer. */
    data class FileExplorer(val initialPath: String? = null) : Route()

    /** Related notes for a book. */
    data class RelatedNotes(val bookId: Long) : Route()
}
