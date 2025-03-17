package com.originb.inkwisenote2.modules.ocr.data

import androidx.room.*

@Dao
interface NoteOcrTextDao {
    @Query("SELECT * FROM note_text WHERE note_id = :noteId")
    fun readTextFromDb(noteId: Long?): NoteOcrText?

    @get:Query("SELECT * FROM note_text")
    val allNoteText: List<NoteOcrText?>?

    @Query("SELECT * FROM note_text WHERE extracted_text LIKE '%' || :searchTerm || '%'")
    fun searchTextFromDb(searchTerm: String?): List<NoteOcrText?>?

    @Update
    fun updateTextToDb(noteOcrText: NoteOcrText?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTextToDb(noteRelation: NoteOcrText?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTextToDb(noteRelation: List<NoteOcrText?>?)

    @Query("DELETE FROM note_text WHERE note_id = :noteId")
    fun deleteNoteText(noteId: Long?)
}
