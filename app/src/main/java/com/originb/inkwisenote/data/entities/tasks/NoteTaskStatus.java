package com.originb.inkwisenote.data.entities.tasks;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "note_task_status", primaryKeys = {"note_id", "task_name"})
public class NoteTaskStatus {
    @ColumnInfo(name = "note_id")
    private long noteId;

    @ColumnInfo(name = "task_name")
    @NonNull
    private NoteTaskName taskName;

    @ColumnInfo(name = "task_stage")
    private NoteTaskStage stage; // values are from TextProcessingStage

    @ColumnInfo(name = "created_time_ms")
    private long createdTimeMillis;

    @ColumnInfo(name = "last_modified_time_ms")
    private long lastModifiedTimeMillis;

    public NoteTaskStatus(long noteId, NoteTaskName taskName, NoteTaskStage stage) {
        this.noteId = noteId;
        this.taskName = taskName;
        this.stage = stage;

        createdTimeMillis = System.currentTimeMillis();
        lastModifiedTimeMillis = System.currentTimeMillis();
    }
}
