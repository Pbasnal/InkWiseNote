package com.originb.inkwisenote.data.dao;

import androidx.room.*;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;

import java.util.List;
import java.util.Set;

@Dao
public interface AtomicNoteEntitiesDao {
    @Query("SELECT * FROM atomic_note_entities WHERE note_id = :noteId")
    AtomicNoteEntity getAtomicNote(Long noteId);

    @Query("SELECT * FROM atomic_note_entities WHERE note_id IN (:noteIds)")
    List<AtomicNoteEntity> getAtomicNotes(Set<Long> noteIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAtomicNote(AtomicNoteEntity atomicNoteEntity);

    @Update
    int updateAtomicNote(AtomicNoteEntity atomicNoteEntity);

    @Query("DELETE FROM atomic_note_entities WHERE note_id = :noteId")
    void deleteAtomicNote(Long noteId);
}
