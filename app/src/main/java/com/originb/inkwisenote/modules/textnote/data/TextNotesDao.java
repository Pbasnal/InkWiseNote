package com.originb.inkwisenote.modules.textnote.data;

import androidx.room.*;

import java.util.List;
import java.util.Set;

@Dao
public interface TextNotesDao {

    @Query("SELECT * FROM text_notes")
    List<TextNoteEntity> getAllTextNotes();

    @Query("SELECT * FROM text_notes WHERE book_id = :bookId")
    TextNoteEntity getTextNoteForBook(Long bookId);

    @Query("SELECT * FROM text_notes WHERE book_id IN (:bookIds)")
    List<TextNoteEntity> getTextNoteForBooks(Set<Long> bookIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTextNote(TextNoteEntity TextNoteEntity);

    @Update
    int updateTextNote(TextNoteEntity TextNoteEntity);

    @Update
    int updateTextNotes(List<TextNoteEntity> TextNoteEntities);

    @Query("DELETE FROM text_notes WHERE book_id = :bookId")
    void deleteTextNotes(long bookId);
}
