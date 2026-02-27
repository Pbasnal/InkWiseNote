package org.basnalcorp.shared.systems.chroniclecore.impl

import org.basnalcorp.shared.systems.chroniclecore.ChronicleCoreDb
import org.basnalcorp.shared.systems.chroniclecore.ChronicleNotebook
import org.basnalcorp.shared.systems.chroniclecore.ChronicleNoteMeta
import org.basnalcorp.shared.db.NotesDatabase

/**
 * ChronicleCore DB implementation using SQLDelight NotesDatabase.
 * Wire this in DI when constructing ChronicleCore.
 */
class ChronicleCoreDbImpl(
    private val db: NotesDatabase
) : ChronicleCoreDb {

    override fun insertNotebook(notebookId: String, displayName: String, creationTime: Long) {
        db.chronicleNotebooksQueries.insertChronicleNotebook(
            notebook_id = notebookId,
            display_name = displayName,
            creation_time = creationTime
        )
    }

    override fun getNotebook(notebookId: String): ChronicleNotebook? {
        return db.chronicleNotebooksQueries.getChronicleNotebook(notebook_id = notebookId)
            .executeAsOneOrNull()
            ?.let { row ->
                ChronicleNotebook(
                    notebookId = row.notebook_id,
                    displayName = row.display_name,
                    creationTime = row.creation_time
                )
            }
    }

    override fun listNotebooks(): List<ChronicleNotebook> {
        return db.chronicleNotebooksQueries.listChronicleNotebooks().executeAsList().map { row ->
            ChronicleNotebook(
                notebookId = row.notebook_id,
                displayName = row.display_name,
                creationTime = row.creation_time
            )
        }
    }

    override fun updateNotebookDisplayName(notebookId: String, displayName: String) {
        db.chronicleNotebooksQueries.updateChronicleNotebookDisplayName(
            display_name = displayName,
            notebook_id = notebookId
        )
    }

    override fun deleteNotebook(notebookId: String) {
        db.chronicleNotebooksQueries.deleteChronicleNotebook(notebook_id = notebookId)
    }

    override fun insertNote(
        noteId: Long,
        notebookId: String,
        title: String,
        creationTime: Long,
        lastModified: Long,
        filePath: String
    ) {
        db.chronicleNotesQueries.insertChronicleNote(
            note_id = noteId,
            notebook_id = notebookId,
            title = title,
            creation_time = creationTime,
            last_modified = lastModified,
            file_path = filePath
        )
    }

    override fun getNote(noteId: Long): ChronicleNoteMeta? {
        return db.chronicleNotesQueries.getChronicleNote(note_id = noteId)
            .executeAsOneOrNull()
            ?.let { row ->
                ChronicleNoteMeta(
                    noteId = row.note_id,
                    notebookId = row.notebook_id,
                    title = row.title,
                    creationTime = row.creation_time,
                    lastModified = row.last_modified,
                    filePath = row.file_path
                )
            }
    }

    override fun listNotes(notebookId: String): List<ChronicleNoteMeta> {
        return db.chronicleNotesQueries.listChronicleNotesForNotebook(notebook_id = notebookId)
            .executeAsList()
            .map { row ->
                ChronicleNoteMeta(
                    noteId = row.note_id,
                    notebookId = row.notebook_id,
                    title = row.title,
                    creationTime = row.creation_time,
                    lastModified = row.last_modified,
                    filePath = row.file_path
                )
            }
    }

    override fun updateNote(noteId: Long, title: String, lastModified: Long, filePath: String) {
        db.chronicleNotesQueries.updateChronicleNote(
            title = title,
            last_modified = lastModified,
            file_path = filePath,
            note_id = noteId
        )
    }

    override fun deleteNote(noteId: Long) {
        db.chronicleNotesQueries.deleteChronicleNote(note_id = noteId)
    }

    override fun deleteNotesForNotebook(notebookId: String) {
        db.chronicleNotesQueries.deleteChronicleNotesForNotebook(notebook_id = notebookId)
    }
}
