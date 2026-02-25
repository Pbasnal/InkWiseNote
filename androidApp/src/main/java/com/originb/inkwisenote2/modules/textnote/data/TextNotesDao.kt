package com.originb.inkwisenote2.modules.textnote.data

import androidx.room.*

@Dao
interface TextNotesDao {
    @get:Query("SELECT * FROM text_notes")
    val allTextNotes: MutableList<TextNoteEntity>

    @Query("SELECT * FROM text_notes WHERE book_id = :bookId")
    fun getTextNoteForBook(bookId: Long): MutableList<TextNoteEntity>

    @Query("SELECT * FROM text_notes WHERE note_id = :noteId")
    fun getTextNoteForNote(noteId: Long): TextNoteEntity

    @Query("SELECT * FROM text_notes WHERE book_id IN (:bookIds)")
    fun getTextNoteForBooks(bookIds: MutableSet<Long>): MutableList<TextNoteEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertTextNote(TextNoteEntity: TextNoteEntity): Long

    @Update
    fun updateTextNote(TextNoteEntity: TextNoteEntity): Int

    @Update
    fun updateTextNotes(TextNoteEntities: MutableList<TextNoteEntity>): Int

    @Query("DELETE FROM text_notes WHERE note_id = :noteId")
    fun deleteTextNote(noteId: Long)

    @Query("DELETE FROM text_notes WHERE book_id = :bookId")
    fun deleteTextNotes(bookId: Long)
}
