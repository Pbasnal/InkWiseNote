package com.originb.inkwisenote2.modules.textnote.data

import androidx.room.*

@Dao
interface TextNotesDao {
    @get:Query("SELECT * FROM text_notes")
    val allTextNotes: List<TextNoteEntity?>?

    @Query("SELECT * FROM text_notes WHERE book_id = :bookId")
    fun getTextNoteForBook(bookId: Long?): TextNoteEntity?

    @Query("SELECT * FROM text_notes WHERE book_id IN (:bookIds)")
    fun getTextNoteForBooks(bookIds: Set<Long?>?): List<TextNoteEntity?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTextNote(TextNoteEntity: TextNoteEntity?): Long

    @Update
    fun updateTextNote(TextNoteEntity: TextNoteEntity?): Int

    @Update
    fun updateTextNotes(TextNoteEntities: List<TextNoteEntity?>?): Int

    @Query("DELETE FROM text_notes WHERE book_id = :bookId")
    fun deleteTextNotes(bookId: Long)
}
