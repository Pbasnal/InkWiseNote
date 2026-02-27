package org.basnalcorp.shared.systems.chroniclecore

/**
 * Public read-model types returned by ChronicleCore read commands.
 * All reads go through the event loop and return these types.
 * Notebook has no creationTime; FS is source of truth for existence.
 */
data class ChronicleNotebook(
    val notebookId: String,
    val noteIds: List<Long>
)

data class ChronicleNoteMeta(
    val noteId: Long,
    val notebookId: String,
    val title: String,
    val creationTime: Long,
    val lastModified: Long,
    val filePath: String
)

data class ChronicleNoteContent(
    val noteId: Long,
    val notebookId: String,
    val title: String,
    val creationTime: Long,
    val lastModified: Long,
    val body: String,
    val unknownFrontmatter: Map<String, String>
)
