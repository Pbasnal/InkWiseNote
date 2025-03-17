package com.originb.inkwisenote2.modules.handwrittennotes.data

import androidx.room.*

@Dao
interface HandwrittenNotesDao {
    @get:Query("SELECT * FROM handwritten_notes")
    val allHandwrittenNotes: List<HandwrittenNoteEntity?>?


    @Query("SELECT * FROM handwritten_notes WHERE note_id = :noteId")
    fun getHandwrittenNoteForNote(noteId: Long?): HandwrittenNoteEntity?

    @Query("SELECT * FROM handwritten_notes WHERE book_id = :bookId")
    fun getHandwrittenNoteForBook(bookId: Long?): List<HandwrittenNoteEntity?>?

    @Query("SELECT * FROM handwritten_notes WHERE book_id IN (:bookIds)")
    fun getHandwrittenNoteForBooks(bookIds: Set<Long?>?): List<HandwrittenNoteEntity?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHandwrittenNote(handwrittenNoteEntity: HandwrittenNoteEntity?): Long

    @Update
    fun updateHandwrittenNote(handwrittenNoteEntity: HandwrittenNoteEntity?): Int

    @Update
    fun updateHandwrittenNotes(handwrittenNoteEntities: List<HandwrittenNoteEntity?>?): Int

    @Query("DELETE FROM handwritten_notes WHERE note_id IN (:noteIds)")
    fun deleteHandwrittenNotes(noteIds: Set<Long?>?)
}
