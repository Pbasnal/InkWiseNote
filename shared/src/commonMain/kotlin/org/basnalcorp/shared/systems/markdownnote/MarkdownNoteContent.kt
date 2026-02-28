package org.basnalcorp.shared.systems.markdownnote

import org.basnalcorp.shared.systems.chroniclecore.ChronicleNoteContent

/**
 * Read-model for a markdown note: Chronicle note content plus [noteType] from frontmatter.
 * Built from [ChronicleNoteContent] with noteType = unknownFrontmatter["note_type"] ?: "markdown".
 */
data class MarkdownNoteContent(
    val noteId: Long,
    val notebookId: String,
    val title: String,
    val body: String,
    val noteType: String,
    val creationTime: Long,
    val lastModified: Long
) {
    companion object {
        private const val DEFAULT_NOTE_TYPE = "markdown"

        fun fromChronicleContent(content: ChronicleNoteContent): MarkdownNoteContent =
            MarkdownNoteContent(
                noteId = content.noteId,
                notebookId = content.notebookId,
                title = content.title,
                body = content.body,
                noteType = content.unknownFrontmatter["note_type"] ?: DEFAULT_NOTE_TYPE,
                creationTime = content.creationTime,
                lastModified = content.lastModified
            )
    }
}
