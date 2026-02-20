package com.originb.inkwisenote2.modules.ocr.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Entity(tableName = "note_text", indices = [Index("note_id")])
class NoteOcrText : Serializable {
    // Getters and Setters
    @JvmField
    @PrimaryKey
    @ColumnInfo(name = "note_id")
    var noteId: Long? = null

    @JvmField
    @ColumnInfo(name = "extracted_text")
    var extractedText: String? = null

    @JvmField
    @ColumnInfo(name = "note_hash")
    var noteHash: String? = null

    @ColumnInfo(name = "created_time_ms")
    private var createdTimeMillis: Long? = null

    @JvmField
    @ColumnInfo(name = "last_modified_time_ms")
    var lastModifiedTimeMillis: Long? = null

    // No-args constructor required by Room
    constructor()

    constructor(noteId: Long?, noteHash: String?, text: String?) {
        this.noteId = noteId
        this.noteHash = noteHash
        this.extractedText = text
        createdTimeMillis = System.currentTimeMillis()
        lastModifiedTimeMillis = System.currentTimeMillis()
    }

    fun getCreatedTimeMillis(): Long {
        return createdTimeMillis!!
    }

    fun setCreatedTimeMillis(createdTimeMillis: Long) {
        this.createdTimeMillis = createdTimeMillis
    }

    val createDateTimeString: String?
        get() {
            val instant = Instant.ofEpochMilli(createdTimeMillis!!)
            val formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault())

            return formatter.format(instant)
        }
}
