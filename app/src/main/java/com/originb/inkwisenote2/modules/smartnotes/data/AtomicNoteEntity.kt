package com.originb.inkwisenote2.modules.smartnotes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "atomic_note_entities")
class AtomicNoteEntity  // No-args constructor required by Room
{
    // Getters and Setters
    @JvmField
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    var noteId: Long = 0

    @JvmField
    @ColumnInfo(name = "filename")
    var filename: String? = null

    @JvmField
    @ColumnInfo(name = "filepath")
    var filepath: String? = null

    @JvmField
    @ColumnInfo(name = "note_type")
    var noteType: String? = null // for handwritten and text

    @JvmField
    @ColumnInfo(name = "page_template_id")
    var pageTemplateId: Long = 0

    @JvmField
    @ColumnInfo(name = "created_time_ms")
    var createdTimeMillis: Long = 0

    @JvmField
    @ColumnInfo(name = "last_modified_time_ms")
    var lastModifiedTimeMillis: Long = 0

    public override fun clone(): AtomicNoteEntity {
        val noteToSave = AtomicNoteEntity()
        noteToSave.noteId = noteId
        noteToSave.filename = filename
        noteToSave.filepath = filepath
        noteToSave.noteType = noteType
        noteToSave.pageTemplateId = pageTemplateId
        noteToSave.createdTimeMillis = createdTimeMillis
        noteToSave.lastModifiedTimeMillis = lastModifiedTimeMillis

        return noteToSave
    }
}


