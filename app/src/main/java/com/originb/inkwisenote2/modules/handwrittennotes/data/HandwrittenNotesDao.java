package com.originb.inkwisenote2.modules.handwrittennotes.data;

import androidx.room.*;

import java.util.List;
import java.util.Set;

@Dao
public interface HandwrittenNotesDao {

    @Query("SELECT * FROM handwritten_notes")
    List<HandwrittenNoteEntity> getAllHandwrittenNotes();


    @Query("SELECT * FROM handwritten_notes WHERE note_id = :noteId")
    HandwrittenNoteEntity getHandwrittenNoteForNote(Long noteId);

    @Query("SELECT * FROM handwritten_notes WHERE book_id = :bookId")
    List<HandwrittenNoteEntity> getHandwrittenNoteForBook(Long bookId);

    @Query("SELECT * FROM handwritten_notes WHERE book_id IN (:bookIds)")
    List<HandwrittenNoteEntity> getHandwrittenNoteForBooks(Set<Long> bookIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertHandwrittenNote(HandwrittenNoteEntity handwrittenNoteEntity);

    @Update
    int updateHandwrittenNote(HandwrittenNoteEntity handwrittenNoteEntity);

    @Update
    int updateHandwrittenNotes(List<HandwrittenNoteEntity> handwrittenNoteEntities);

    @Query("DELETE FROM handwritten_notes WHERE note_id IN (:noteIds)")
    void deleteHandwrittenNotes(Set<Long> noteIds);
}
