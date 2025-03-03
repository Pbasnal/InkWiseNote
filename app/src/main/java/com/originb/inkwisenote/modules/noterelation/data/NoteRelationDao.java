package com.originb.inkwisenote.modules.noterelation.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import java.util.Set;

@Dao
public interface NoteRelationDao {
    @Query("SELECT * FROM note_relation ")
    List<NoteRelation> getAllNoteRelations();

    @Query("SELECT * FROM note_relation WHERE note_id = :noteId or related_note_id = :noteId")
    List<NoteRelation> getRelatedNotesOf(Long noteId);

    @Query("SELECT * FROM note_relation WHERE note_id IN (:noteIds) OR related_note_id IN (:noteIds)")
    List<NoteRelation> getRelatedNotesOf(Set<Long> noteIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNoteRelatedNotes(Set<NoteRelation> noteRelation);

    // Alternatively, for custom delete queries, use @Query
    @Query("DELETE FROM note_relation WHERE note_id IN (:noteIds) or related_note_id IN (:noteIds)")
    void deleteByNoteId(List<Long> noteIds);

    // Alternatively, for custom delete queries, use @Query
    @Query("DELETE FROM note_relation WHERE note_id = :noteId or related_note_id = :noteId")
    void deleteByNoteId(Long noteId);

}
