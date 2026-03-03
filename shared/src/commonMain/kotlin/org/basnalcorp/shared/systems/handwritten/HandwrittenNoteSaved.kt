package org.basnalcorp.shared.systems.handwritten

/**
 * Emitted by the integration layer when a handwritten note is saved.
 * Not part of ChronicleCore; subscribe via callback or SharedFlow in the host.
 */
data class HandwrittenNoteSaved(
    val noteId: Long,
    val notebookId: String,
    val isAutosave: Boolean
)
