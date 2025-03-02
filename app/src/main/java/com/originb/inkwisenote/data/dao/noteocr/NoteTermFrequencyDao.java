package com.originb.inkwisenote.data.dao.noteocr;

import androidx.room.*;
import com.originb.inkwisenote.data.entities.noteocrdata.NoteTermFrequency;
import com.originb.inkwisenote.data.entities.noteocrdata.TermOccurrence;

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTermFrequenciesToDb(List<NoteTermFrequency> noteRelation);

    @Query("SELECT term, COUNT(*) as occurrenceCount FROM note_term_frequency WHERE term IN (:terms) GROUP BY term")
    List<TermOccurrence> getTermOccurrences(Set<String> terms);

    @Query("SELECT COUNT(DISTINCT note_id) as distinct_count FROM note_term_frequency")
    int getDistinctNoteIdCount();

    @Query("DELETE FROM note_term_frequency WHERE note_id = :noteId")
    void deleteTermFrequencies(Long noteId);
}
