package org.basnalcorp.shared.data.repository.impl

import org.basnalcorp.shared.data.repository.NoteTermFrequencyRepository
import org.basnalcorp.shared.db.NotesDatabase
import org.basnalcorp.shared.domain.NoteTermFrequency

class NoteTermFrequencyRepositoryImpl(private val db: NotesDatabase) : NoteTermFrequencyRepository {

    override fun insertTermFrequencies(entries: List<NoteTermFrequency>) {
        entries.forEach { e ->
            db.noteTermFrequencyQueries.insertTermFrequencies(
                note_id = e.noteId,
                term = e.term,
                fq_in_doc = e.termFrequency
            )
        }
    }

    override fun deleteForNote(noteId: Long) {
        db.noteTermFrequencyQueries.deleteTermFrequenciesForNote(note_id = noteId)
    }

    override fun getTermFrequenciesForNote(noteId: Long): List<NoteTermFrequency> =
        db.noteTermFrequencyQueries.readTermFrequenciesOfNote(note_id = noteId).executeAsList().map { it.toDomain() }

    override fun getDistinctNoteIdCount(): Long =
        db.noteTermFrequencyQueries.distinctNoteIdCount().executeAsOne()

    override fun getNoteIdsForTerms(terms: Set<String>): Map<String, Set<Long>> {
        if (terms.isEmpty()) return emptyMap()
        val rows = db.noteTermFrequencyQueries.getNoteIdsForTerms(terms).executeAsList()
        val result = mutableMapOf<String, MutableSet<Long>>()
        rows.forEach { row ->
            result.getOrPut(row.term) { mutableSetOf() }.add(row.note_id)
        }
        return result
    }
}

private fun org.basnalcorp.shared.db.Note_term_frequency.toDomain() = NoteTermFrequency(
    id = id,
    noteId = note_id,
    term = term,
    termFrequency = fq_in_doc
)
