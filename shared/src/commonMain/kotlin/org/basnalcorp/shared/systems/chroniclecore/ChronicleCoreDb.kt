package org.basnalcorp.shared.systems.chroniclecore

/**
 * Index database interface for ChronicleCore. All persistence for the read model
 * goes through this interface. Implementations may use SQLDelight, Room, or any other store.
 */
interface ChronicleCoreDb {

    fun insertNotebook(notebookId: String, displayName: String, creationTime: Long)

    fun getNotebook(notebookId: String): ChronicleNotebook?

    fun listNotebooks(): List<ChronicleNotebook>

    fun updateNotebookDisplayName(notebookId: String, displayName: String)

    fun deleteNotebook(notebookId: String)

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

    fun updateNote(noteId: Long, title: String, lastModified: Long, filePath: String)

    fun deleteNote(noteId: Long)

    fun deleteNotesForNotebook(notebookId: String)
}
