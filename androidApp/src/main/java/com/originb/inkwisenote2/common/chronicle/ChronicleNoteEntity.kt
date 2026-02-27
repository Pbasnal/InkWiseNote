package com.originb.inkwisenote2.common.chronicle

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chronicle_notes")
class ChronicleNoteEntity {

    @PrimaryKey
    @ColumnInfo(name = "note_id")
    var noteId: Long = 0L

    @ColumnInfo(name = "notebook_id")
    var notebookId: String = ""

    @ColumnInfo(name = "title")
    var title: String = ""

    @ColumnInfo(name = "creation_time")
    var creationTime: Long = 0L

    @ColumnInfo(name = "last_modified")
    var lastModified: Long = 0L

    @ColumnInfo(name = "file_path")
    var filePath: String = ""

    constructor()
    constructor(
        noteId: Long,
        notebookId: String,
        title: String,
        creationTime: Long,
        lastModified: Long,
        filePath: String
    ) {
        this.noteId = noteId
        this.notebookId = notebookId
        this.title = title
        this.creationTime = creationTime
        this.lastModified = lastModified
        this.filePath = filePath
    }
}
