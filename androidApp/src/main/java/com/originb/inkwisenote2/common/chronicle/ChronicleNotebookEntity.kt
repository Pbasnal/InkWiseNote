package com.originb.inkwisenote2.common.chronicle

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chronicle_notebooks")
class ChronicleNotebookEntity {

    @PrimaryKey
    @ColumnInfo(name = "notebook_id")
    var notebookId: String = ""

    @ColumnInfo(name = "display_name")
    var displayName: String = ""

    @ColumnInfo(name = "creation_time")
    var creationTime: Long = 0L

    constructor()
    constructor(notebookId: String, displayName: String, creationTime: Long) {
        this.notebookId = notebookId
        this.displayName = displayName
        this.creationTime = creationTime
    }
}
