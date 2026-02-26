package org.basnalcorp.shared.data.repository.impl

import org.basnalcorp.shared.data.repository.TextNotesRepository
import org.basnalcorp.shared.db.NotesDatabase
import org.basnalcorp.shared.domain.TextNote
import kotlinx.datetime.Clock

class TextNotesRepositoryImpl(private val db: NotesDatabase) : TextNotesRepository {

    override fun getForNote(noteId: Long): TextNote? {
        return db.textNotesQueries.getTextNoteForNote(note_id = noteId).executeAsOneOrNull()?.toDomain()
    }

    override fun insertOrReplace(note: TextNote) {
        db.textNotesQueries.insertOrReplaceTextNote(
            note_id = note.noteId,
            book_id = note.bookId,
            note_text = note.noteText,
            created_time_ms = note.createdTimeMillis,
            last_modified_time_ms = note.lastModifiedTimeMillis
        )
    }

    override fun updateText(noteId: Long, noteText: String?, lastModifiedTimeMs: Long) {
        db.textNotesQueries.updateTextNote(
            note_text = noteText,
            last_modified_time_ms = lastModifiedTimeMs,
            note_id = noteId
        )
    }

    override fun delete(noteId: Long) {
        db.textNotesQueries.deleteTextNote(note_id = noteId)
    }
}

private fun org.basnalcorp.shared.db.Text_notes.toDomain() = TextNote(
    noteId = note_id,
    bookId = book_id,
    noteText = note_text,
    createdTimeMillis = created_time_ms,
    lastModifiedTimeMillis = last_modified_time_ms
)
