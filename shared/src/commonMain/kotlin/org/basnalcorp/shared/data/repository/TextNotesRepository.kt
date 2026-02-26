package org.basnalcorp.shared.data.repository

import org.basnalcorp.shared.domain.TextNote

interface TextNotesRepository {
    fun getForNote(noteId: Long): TextNote?
    fun insertOrReplace(note: TextNote)
    fun updateText(noteId: Long, noteText: String?, lastModifiedTimeMs: Long)
    fun delete(noteId: Long)
}
