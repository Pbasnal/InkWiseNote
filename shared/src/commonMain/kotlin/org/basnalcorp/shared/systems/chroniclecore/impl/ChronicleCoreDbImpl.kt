package org.basnalcorp.shared.systems.chroniclecore.impl

import org.basnalcorp.shared.systems.chroniclecore.ChronicleCoreDb
import org.basnalcorp.shared.systems.chroniclecore.ChronicleNoteMeta
import org.basnalcorp.shared.db.NotesDatabase

/**
 * ChronicleCore DB implementation using SQLDelight NotesDatabase.
 * Wire this in DI when constructing ChronicleCore.
 * Notebook table removed; FS is source of truth for notebook existence.
 */
class ChronicleCoreDbImpl(
    private val db: NotesDatabase
) : ChronicleCoreDb {

    override fun updateNotesNotebookId(newNotebookId: String, oldNotebookId: String) {
        db.chronicleNotesQueries.updateNotesNotebookId(
            newNotebookId,
            oldNotebookId
        )
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

    override fun listNotebookIdsInDb(): List<String> {
        return db.chronicleNotesQueries.listDistinctNotebookIds().executeAsList().map { it }
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
