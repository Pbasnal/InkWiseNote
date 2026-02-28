package org.basnalcorp.shared.systems.markdownnote

import org.basnalcorp.shared.systems.chroniclecore.ChronicleCommandResult
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCore
import org.basnalcorp.shared.systems.chroniclecore.ChronicleNoteContent

/**
 * Thin layer over ChronicleCore that ensures [note_type] in frontmatter for markdown notes
 * and delegates all persistence to ChronicleCore.
 */
class MarkdownNoteSystem(
    private val chronicleCore: ChronicleCore
) {
    suspend fun createNote(
        notebookId: String,
        title: String,
        body: String,
        noteType: String = DEFAULT_NOTE_TYPE
    ): ChronicleCommandResult<ChronicleNoteContent> =
        chronicleCore.createNote(
            notebookId = notebookId,
            title = title,
            body = body,
            optionalFrontmatter = mapOf(NOTE_TYPE_KEY to noteType)
        )

    suspend fun updateNote(
        noteId: Long,
        notebookId: String,
        title: String,
        body: String,
        expectedLastModified: Long,
        noteType: String = DEFAULT_NOTE_TYPE
    ): ChronicleCommandResult<ChronicleNoteContent> =
        chronicleCore.updateNote(
            noteId = noteId,
            notebookId = notebookId,
            updatedTitle = title,
            updatedBody = body,
            expectedLastModified = expectedLastModified,
            preserveUnknownKeys = mapOf(NOTE_TYPE_KEY to noteType)
        )

    suspend fun getNote(notebookId: String, noteId: Long): MarkdownNoteContent? =
        chronicleCore.getNote(notebookId, noteId)?.let { MarkdownNoteContent.fromChronicleContent(it) }

    companion object {
        private const val DEFAULT_NOTE_TYPE = "markdown"
        private const val NOTE_TYPE_KEY = "note_type"
    }
}
