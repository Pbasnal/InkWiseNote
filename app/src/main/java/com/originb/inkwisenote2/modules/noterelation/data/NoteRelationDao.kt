package com.originb.inkwisenote2.modules.noterelation.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NoteRelationDao {
    @get:Query("SELECT * FROM note_relation ")
    val allNoteRelations: List<NoteRelation?>?

    @Query("SELECT * FROM note_relation WHERE note_id = :noteId or related_note_id = :noteId")
    fun getRelatedNotesOf(noteId: Long?): List<NoteRelation?>?

    @Query("SELECT * FROM note_relation WHERE note_id IN (:noteIds) OR related_note_id IN (:noteIds)")
    fun getRelatedNotesOf(noteIds: Set<Long>?): List<NoteRelation?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNoteRelatedNotes(noteRelation: Set<NoteRelation?>?)

    // Alternatively, for custom delete queries, use @Query
    @Query("DELETE FROM note_relation WHERE note_id IN (:noteIds) or related_note_id IN (:noteIds)")
    fun deleteByNoteId(noteIds: List<Long>?)

    // Alternatively, for custom delete queries, use @Query
    @Query("DELETE FROM note_relation WHERE note_id = :noteId or related_note_id = :noteId")
    fun deleteByNoteId(noteId: Long?)
}
