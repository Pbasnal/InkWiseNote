package com.originb.inkwisenote.data.dao.tasks;

import androidx.room.*;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStatus;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskName;

@Dao
public interface NoteTaskStatusDao {
    @Query("SELECT * FROM note_task_status where note_id = :noteId and task_name = :taskName")
    NoteTaskStatus getNoteStatus(long noteId, NoteTaskName taskName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNoteTask(NoteTaskStatus noteTaskStatus);

    @Update
    void updateNoteTask(NoteTaskStatus noteTaskStatus);

    @Query("DELETE FROM note_task_status WHERE note_id = :noteId and task_name = :taskName")
    void deleteNoteTask(Long noteId, NoteTaskName taskName);
}
