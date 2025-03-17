package com.originb.inkwisenote2.modules.smartnotes.data

import androidx.room.*

@Dao
interface AtomicNoteEntitiesDao {
    @Query("SELECT * FROM atomic_note_entities WHERE note_id = :noteId")
    fun getAtomicNote(noteId: Long?): AtomicNoteEntity?

    @get:Query("SELECT * FROM atomic_note_entities")
    val allAtomicNotes: List<AtomicNoteEntity?>?

    @Query("SELECT * FROM atomic_note_entities WHERE note_id IN (:noteIds)")
    fun getAtomicNotes(noteIds: Set<Long>?): MutableList<AtomicNoteEntity?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAtomicNote(atomicNoteEntity: AtomicNoteEntity?): Long

    @Update
    fun updateAtomicNote(atomicNoteEntity: AtomicNoteEntity?): Int

    @Update
    fun updateAtomicNote(atomicNoteEntity: List<AtomicNoteEntity?>?): Int

    @Query("DELETE FROM atomic_note_entities WHERE note_id = :noteId")
    fun deleteAtomicNote(noteId: Long?)
}
