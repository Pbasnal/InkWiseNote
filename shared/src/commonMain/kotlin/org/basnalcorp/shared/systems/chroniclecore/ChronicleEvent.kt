package org.basnalcorp.shared.systems.chroniclecore

/**
 * Domain events emitted after successful ChronicleCore commits.
 * Ordered, in-process, at-most-once; not persisted.
 */
sealed class ChronicleEvent {

    data class NotebookCreated(
        val notebookId: String,
        val displayName: String,
        val creationTime: Long
    ) : ChronicleEvent()

    data class NotebookRenamed(
        val notebookId: String,
        val newDisplayName: String
    ) : ChronicleEvent()

    data class NotebookDeleted(
        val notebookId: String
    ) : ChronicleEvent()

    data class NoteCreated(
        val noteId: Long,
        val notebookId: String,
        val creationTime: Long,
        val lastModified: Long
    ) : ChronicleEvent()

    data class NoteUpdated(
        val noteId: Long,
        val notebookId: String,
        val lastModified: Long
    ) : ChronicleEvent()

    data class NoteDeleted(
        val noteId: Long,
        val notebookId: String,
        val lastModified: Long
    ) : ChronicleEvent()
}
