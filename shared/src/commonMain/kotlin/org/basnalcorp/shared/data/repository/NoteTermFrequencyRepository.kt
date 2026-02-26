package org.basnalcorp.shared.data.repository

import org.basnalcorp.shared.domain.NoteTermFrequency

interface NoteTermFrequencyRepository {
    fun insertTermFrequencies(entries: List<NoteTermFrequency>)
    fun deleteForNote(noteId: Long)
    fun getTermFrequenciesForNote(noteId: Long): List<NoteTermFrequency>
    fun getDistinctNoteIdCount(): Long
    /** Returns map of term -> set of note IDs that contain that term. */
    fun getNoteIdsForTerms(terms: Set<String>): Map<String, Set<Long>>
}
