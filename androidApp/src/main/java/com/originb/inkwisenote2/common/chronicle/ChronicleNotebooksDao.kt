package com.originb.inkwisenote2.common.chronicle

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChronicleNotebooksDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ChronicleNotebookEntity)

    @Query("SELECT * FROM chronicle_notebooks WHERE notebook_id = :notebookId")
    fun get(notebookId: String): ChronicleNotebookEntity?

    @Query("SELECT * FROM chronicle_notebooks ORDER BY creation_time DESC")
    fun listAll(): List<ChronicleNotebookEntity>

    @Query("UPDATE chronicle_notebooks SET display_name = :displayName WHERE notebook_id = :notebookId")
    fun updateDisplayName(notebookId: String, displayName: String)

    @Query("DELETE FROM chronicle_notebooks WHERE notebook_id = :notebookId")
    fun delete(notebookId: String)
}
