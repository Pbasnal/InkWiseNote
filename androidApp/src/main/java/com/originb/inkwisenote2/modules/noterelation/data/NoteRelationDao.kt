package com.originb.inkwisenote2.modules.noterelation.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NoteRelationDao {
    @get:Query("SELECT * FROM note_relation ")
    val allNoteRelations: MutableList<NoteRelation>

    @Query("SELECT * FROM note_relation WHERE note_id = :noteId or related_note_id = :noteId")
    fun getRelatedNotesOf(noteId: Long): MutableList<NoteRelation>

    @Query("SELECT * FROM note_relation WHERE note_id IN (:noteIds) OR related_note_id IN (:noteIds)")
    fun getRelatedNotesOf(noteIds: MutableSet<Long>): MutableList<NoteRelation>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertNoteRelatedNotes(noteRelation: MutableSet<NoteRelation>)

    // Alternatively, for custom delete queries, use @Query
    @Query("DELETE FROM note_relation WHERE note_id IN (:noteIds) or related_note_id IN (:noteIds)")
    fun deleteByNoteId(noteIds: MutableList<Long>)

    // Alternatively, for custom delete queries, use @Query
    @Query("DELETE FROM note_relation WHERE note_id = :noteId or related_note_id = :noteId")
    fun deleteByNoteId(noteId: Long)
}
