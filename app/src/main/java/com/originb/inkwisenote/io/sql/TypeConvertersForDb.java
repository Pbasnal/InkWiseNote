package com.originb.inkwisenote.io.sql;

import androidx.room.TypeConverter;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskName;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStage;

public class TypeConvertersForDb {
    @TypeConverter
    public static String fromNoteTaskName(NoteTaskName taskName) {
        return taskName.toString();
    }

    @TypeConverter
    public static NoteTaskName toNoteTaskName(String taskName) {

        return NoteTaskName.valueOf(taskName);
    }

    @TypeConverter
    public static String fromNoteTaskStage(NoteTaskStage taskStage) {
        return taskStage.toString();
    }

    @TypeConverter
    public static NoteTaskStage toNoteTaskStage(String taskStage) {
        return NoteTaskStage.valueOf(taskStage);
    }
}