package com.originb.inkwisenote2.modules.ocr.data

import androidx.room.*

@Dao
interface NoteOcrTextsDao {
    @Query("SELECT * FROM note_text WHERE note_id = :noteId")
    fun readTextFromDb(noteId: Long): NoteOcrText

    @get:Query("SELECT * FROM note_text")
    val allNoteText: MutableList<NoteOcrText>

    @Query("SELECT * FROM note_text WHERE extracted_text LIKE '%' || :searchTerm || '%'")
    fun searchTextFromDb(searchTerm: String): MutableList<NoteOcrText>

    @Update
    fun updateTextToDb(noteOcrText: NoteOcrText): Int

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertTextToDb(noteRelation: NoteOcrText)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertTextToDb(noteRelation: MutableList<NoteOcrText>)

    @Query("DELETE FROM note_text WHERE note_id = :noteId")
    fun deleteNoteText(noteId: Long)
}
