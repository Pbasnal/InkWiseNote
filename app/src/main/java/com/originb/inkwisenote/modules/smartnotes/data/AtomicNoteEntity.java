package com.originb.inkwisenote.modules.smartnotes.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "atomic_note_entities")
public class AtomicNoteEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    private long noteId;

    @ColumnInfo(name = "filename")
    private String filename;

    @ColumnInfo(name = "filepath")
    private String filepath;

    @ColumnInfo(name = "note_type")
    private String noteType; // for handwritten and text

    @ColumnInfo(name = "page_template_id")
    private long pageTemplateId;

    @ColumnInfo(name = "created_time_ms")
    private long createdTimeMillis;

    @ColumnInfo(name = "last_modified_time_ms")
    private long lastModifiedTimeMillis;
}




















