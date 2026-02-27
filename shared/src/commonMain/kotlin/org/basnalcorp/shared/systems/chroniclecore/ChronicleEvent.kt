package org.basnalcorp.shared.systems.chroniclecore

/**
 * Domain events emitted after ChronicleCore commits (success or failure).
 * Ordered, in-process, at-most-once; not persisted.
 * UI can use failure events to show alerts.
 */
sealed class ChronicleEvent {

    data class NotebookCreated(
        val notebookId: String
    ) : ChronicleEvent()

    data class NotebookRenamed(
        val oldNotebookId: String,
        val newNotebookId: String
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

    /** Emitted when a mutation fails so UI can show an alert. */
    data class OperationFailed(
        val operation: String,
        val message: String
    ) : ChronicleEvent()
}
