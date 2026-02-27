package org.basnalcorp.shared.systems.chroniclecore

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * All commands processed by ChronicleCore on the single-threaded event loop.
 * Mutations carry CompletableDeferred<ChronicleCommandResult<T>>; reads carry CompletableDeferred for the result.
 * FS is source of truth; no separate sync-only commands.
 */
sealed interface ChronicleCommand {

    data class CreateNotebook(
        val name: String,
        val reply: CompletableDeferred<ChronicleCommandResult<ChronicleNotebook>>
    ) : ChronicleCommand

    data class RenameNotebook(
        val notebookId: String,
        val newNotebookId: String,
        val reply: CompletableDeferred<ChronicleCommandResult<ChronicleNotebook>>
    ) : ChronicleCommand

    data class DeleteNotebook(
        val notebookId: String,
        val reply: CompletableDeferred<ChronicleCommandResult<Unit>>
    ) : ChronicleCommand

    data class CreateNote(
        val notebookId: String,
        val title: String,
        val body: String,
        val optionalFrontmatter: Map<String, String> = emptyMap(),
        /** When non-null (resync path): file must exist; we only ensure DB row from file. When null: file must not exist. */
        val optionalNoteId: Long? = null,
        val reply: CompletableDeferred<ChronicleCommandResult<ChronicleNoteContent>>
    ) : ChronicleCommand

    data class UpdateNote(
        val noteId: Long,
        val notebookId: String,
        val updatedTitle: String,
        val updatedBody: String,
        val expectedLastModified: Long,
        val preserveUnknownKeys: Map<String, String> = emptyMap(),
        val reply: CompletableDeferred<ChronicleCommandResult<ChronicleNoteContent>>
    ) : ChronicleCommand

    data class DeleteNote(
        val noteId: Long,
        val notebookId: String,
        val expectedLastModified: Long,
        val reply: CompletableDeferred<ChronicleCommandResult<Unit>>
    ) : ChronicleCommand

    data class Resync(
        val notebookIds: List<String>,
        val reply: CompletableDeferred<ReceiveChannel<SyncStatus>>
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

    /** Internal: get a single notebook by id (for resync). FS is source of truth. */
    data class GetNotebook(
        val notebookId: String,
        val reply: CompletableDeferred<ChronicleNotebook?>
    ) : ChronicleCommand
}
