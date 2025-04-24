package com.originb.inkwisenote2.modules.ocr.data;

import androidx.room.*;

import java.util.List;
import java.util.Set;

@Dao
public interface NoteTermFrequencyDao {
    @Query("SELECT * FROM note_term_frequency WHERE note_id = :noteIdToRead")
    List<NoteTermFrequency> readTermFrequenciesOfNote(Long noteIdToRead);

    @Query("SELECT * FROM note_term_frequency")
    List<NoteTermFrequency> getAllTermFrequencies();

    @Query("SELECT * FROM note_term_frequency WHERE term IN (:terms)")
    List<NoteTermFrequency> getNoteIdsForTerms(Set<String> terms);

    @Query("SELECT * FROM note_term_frequency WHERE term NOT IN (:terms)")
    List<NoteTermFrequency> getNoteIdsForAllTermsExcept(Set<String> terms);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTermFrequenciesToDb(List<NoteTermFrequency> noteRelation);

    @Query("SELECT term, COUNT(*) as occurrenceCount FROM note_term_frequency WHERE term IN (:terms) GROUP BY term")
    List<TermOccurrence> getTermOccurrences(Set<String> terms);

    @Query("SELECT COUNT(DISTINCT note_id) as distinct_count FROM note_term_frequency")
    int getDistinctNoteIdCount();

    @Query("DELETE FROM note_term_frequency WHERE note_id = :noteId")
    void deleteTermFrequencies(Long noteId);
}
