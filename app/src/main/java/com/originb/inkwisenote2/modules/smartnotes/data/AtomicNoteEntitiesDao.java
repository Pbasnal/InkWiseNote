package com.originb.inkwisenote2.modules.smartnotes.data;

import androidx.room.*;

import java.util.List;
import java.util.Set;

@Dao
public interface AtomicNoteEntitiesDao {
    @Query("SELECT * FROM atomic_note_entities WHERE note_id = :noteId")
    AtomicNoteEntity getAtomicNote(Long noteId);

    @Query("SELECT * FROM atomic_note_entities")
    List<AtomicNoteEntity> getAllAtomicNotes();

    @Query("SELECT * FROM atomic_note_entities WHERE note_id IN (:noteIds)")
    List<AtomicNoteEntity> getAtomicNotes(Set<Long> noteIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAtomicNote(AtomicNoteEntity atomicNoteEntity);

    @Update
    int updateAtomicNote(AtomicNoteEntity atomicNoteEntity);

    @Update
    int updateAtomicNotes(List<AtomicNoteEntity> atomicNoteEntity);

    @Query("DELETE FROM atomic_note_entities WHERE note_id = :noteId")
    void deleteAtomicNote(Long noteId);
}
