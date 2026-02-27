package com.originb.inkwisenote2.common.chronicle

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChronicleNotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ChronicleNoteEntity)

    @Query("SELECT * FROM chronicle_notes WHERE note_id = :noteId")
    fun get(noteId: Long): ChronicleNoteEntity?

    @Query("SELECT * FROM chronicle_notes WHERE notebook_id = :notebookId ORDER BY creation_time DESC")
    fun listForNotebook(notebookId: String): List<ChronicleNoteEntity>

    @Update
    fun update(entity: ChronicleNoteEntity)

    @Query("UPDATE chronicle_notes SET title = :title, last_modified = :lastModified, file_path = :filePath WHERE note_id = :noteId")
    fun updateFields(noteId: Long, title: String, lastModified: Long, filePath: String)

    @Query("DELETE FROM chronicle_notes WHERE note_id = :noteId")
    fun delete(noteId: Long)

    @Query("DELETE FROM chronicle_notes WHERE notebook_id = :notebookId")
    fun deleteForNotebook(notebookId: String)
}
