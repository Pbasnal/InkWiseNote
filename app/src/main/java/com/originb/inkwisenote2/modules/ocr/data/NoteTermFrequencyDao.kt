package com.originb.inkwisenote2.modules.ocr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NoteTermFrequencyDao {
    @Query("SELECT * FROM note_term_frequency WHERE note_id = :noteIdToRead")
    fun readTermFrequenciesOfNote(noteIdToRead: Long?): List<NoteTermFrequency?>?

    @get:Query("SELECT * FROM note_term_frequency")
    val allTermFrequencies: List<NoteTermFrequency?>?

    @Query("SELECT * FROM note_term_frequency WHERE term IN (:terms)")
    fun getNoteIdsForTerms(terms: Set<String?>?): List<NoteTermFrequency?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTermFrequenciesToDb(noteRelation: List<NoteTermFrequency>?)

    @Query("SELECT term, COUNT(*) as occurrenceCount FROM note_term_frequency WHERE term IN (:terms) GROUP BY term")
    fun getTermOccurrences(terms: Set<String?>?): List<TermOccurrence?>?

    @get:Query("SELECT COUNT(DISTINCT note_id) as distinct_count FROM note_term_frequency")
    val distinctNoteIdCount: Int

    @Query("DELETE FROM note_term_frequency WHERE note_id = :noteId")
    fun deleteTermFrequencies(noteId: Long?)
}
