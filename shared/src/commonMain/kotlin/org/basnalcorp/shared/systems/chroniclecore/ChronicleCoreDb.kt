package org.basnalcorp.shared.systems.chroniclecore

/**
 * Index database interface for ChronicleCore. All persistence for the read model
 * goes through this interface. Notebook existence is derived from FS (source of truth);
 * this interface only stores note metadata.
 */
interface ChronicleCoreDb {

    /** Updates all notes belonging to [oldNotebookId] to [newNotebookId]. Used for rename. */
    fun updateNotesNotebookId(newNotebookId: String, oldNotebookId: String)

    fun insertNote(
        noteId: Long,
        notebookId: String,
        title: String,
        creationTime: Long,
        lastModified: Long,
        filePath: String
    )

    fun getNote(noteId: Long): ChronicleNoteMeta?

    fun listNotes(notebookId: String): List<ChronicleNoteMeta>

    /** Notebook ids that have at least one note in DB (for resync DB cleanup). */
    fun listNotebookIdsInDb(): List<String>

    fun updateNote(noteId: Long, title: String, lastModified: Long, filePath: String)

    fun deleteNote(noteId: Long)

    fun deleteNotesForNotebook(notebookId: String)
}
