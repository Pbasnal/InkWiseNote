package com.originb.inkwisenote2.modules.ocr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NoteTermFrequencyDao {
    @Query("SELECT * FROM note_term_frequency WHERE note_id = :noteIdToRead")
    fun readTermFrequenciesOfNote(noteIdToRead: Long): MutableList<NoteTermFrequency>

    @JvmField
    @get:Query("SELECT * FROM note_term_frequency")
    val allTermFrequencies: MutableList<NoteTermFrequency>

    @Query("SELECT * FROM note_term_frequency WHERE term IN (:terms)")
    fun getNoteIdsForTerms(terms: MutableSet<String>): MutableList<NoteTermFrequency>

    @Query("SELECT * FROM note_term_frequency WHERE term NOT IN (:terms)")
    fun getNoteIdsForAllTermsExcept(terms: MutableSet<String>): MutableList<NoteTermFrequency>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertTermFrequenciesToDb(noteRelation: MutableList<NoteTermFrequency>)

    @Query("SELECT term, COUNT(*) as occurrenceCount FROM note_term_frequency WHERE term IN (:terms) GROUP BY term")
    fun getTermOccurrences(terms: MutableSet<String>): MutableList<TermOccurrence>

    @JvmField
    @get:Query("SELECT COUNT(DISTINCT note_id) as distinct_count FROM note_term_frequency")
    val distinctNoteIdCount: Int

    @Query("DELETE FROM note_term_frequency WHERE note_id = :noteId")
    fun deleteTermFrequencies(noteId: Long)
}
