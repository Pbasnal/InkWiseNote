package org.basnalcorp.shared.data.repository.impl

import org.basnalcorp.shared.data.repository.AtomicNotesRepository
import org.basnalcorp.shared.db.NotesDatabase
import org.basnalcorp.shared.domain.AtomicNote

class AtomicNotesRepositoryImpl(private val db: NotesDatabase) : AtomicNotesRepository {

    override fun insert(atomicNote: AtomicNote): Long {
        db.atomicNoteEntitiesQueries.insertAtomicNote(
            filename = atomicNote.filename,
            filepath = atomicNote.filepath,
            note_type = atomicNote.noteType,
            page_template_id = atomicNote.pageTemplateId,
            created_time_ms = atomicNote.createdTimeMillis,
            last_modified_time_ms = atomicNote.lastModifiedTimeMillis
        )
        return db.lastInsertRowIdQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(atomicNote: AtomicNote): Int {
        db.atomicNoteEntitiesQueries.updateAtomicNote(
            filename = atomicNote.filename,
            filepath = atomicNote.filepath,
            note_type = atomicNote.noteType,
            page_template_id = atomicNote.pageTemplateId,
            created_time_ms = atomicNote.createdTimeMillis,
            last_modified_time_ms = atomicNote.lastModifiedTimeMillis,
            note_id = atomicNote.noteId
        )
        return 1
    }

    override fun delete(noteId: Long) {
        db.atomicNoteEntitiesQueries.deleteAtomicNote(note_id = noteId)
    }

    override fun get(noteId: Long): AtomicNote? {
        return db.atomicNoteEntitiesQueries.getAtomicNote(note_id = noteId).executeAsOneOrNull()?.toDomain()
    }

    override fun getByIds(noteIds: Set<Long>): List<AtomicNote> {
        if (noteIds.isEmpty()) return emptyList()
        return db.atomicNoteEntitiesQueries.getAtomicNotesByIds(noteIds).executeAsList().map { it.toDomain() }
    }
}

private fun org.basnalcorp.shared.db.Atomic_note_entities.toDomain() = AtomicNote(
    noteId = note_id,
    filename = filename,
    filepath = filepath,
    noteType = note_type,
    pageTemplateId = page_template_id,
    createdTimeMillis = created_time_ms,
    lastModifiedTimeMillis = last_modified_time_ms
)

