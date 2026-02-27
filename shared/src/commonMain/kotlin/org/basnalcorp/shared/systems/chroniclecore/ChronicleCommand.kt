package org.basnalcorp.shared.systems.chroniclecore

import kotlinx.coroutines.CompletableDeferred

/**
 * All commands processed by ChronicleCore on the single-threaded event loop.
 * Mutations have no reply; reads carry CompletableDeferred for the result.
 */
sealed interface ChronicleCommand {

    data class CreateNotebook(val name: String?) : ChronicleCommand

    data class RenameNotebook(
        val notebookId: String,
        val newDisplayName: String
    ) : ChronicleCommand

    data class DeleteNotebook(val notebookId: String) : ChronicleCommand

    data class CreateNote(
        val notebookId: String,
        val title: String,
        val body: String,
        val optionalFrontmatter: Map<String, String>? = null
    ) : ChronicleCommand

    data class UpdateNote(
        val noteId: Long,
        val notebookId: String,
        val updatedTitle: String,
        val updatedBody: String,
        val expectedLastModified: Long,
        val preserveUnknownKeys: Map<String, String> = emptyMap()
    ) : ChronicleCommand

    data class DeleteNote(
        val noteId: Long,
        val notebookId: String,
        val expectedLastModified: Long
    ) : ChronicleCommand

    data class ListNotebooks(
        val reply: CompletableDeferred<List<ChronicleNotebook>>
    ) : ChronicleCommand

    data class ListNotes(
        val notebookId: String,
        val reply: CompletableDeferred<List<ChronicleNoteMeta>>
    ) : ChronicleCommand

    data class GetNote(
        val notebookId: String,
        val noteId: Long,
        val reply: CompletableDeferred<ChronicleNoteContent?>
    ) : ChronicleCommand
}
