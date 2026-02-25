package com.originb.inkwisenote2.modules.textnote.data

import androidx.room.*
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity

@Entity(
    tableName = "text_notes",
    foreignKeys = [ForeignKey(
        entity = SmartBookEntity::class,
        parentColumns = ["book_id"],
        childColumns = ["book_id"],
        onDelete = ForeignKey.Companion.CASCADE
    ), ForeignKey(
        entity = AtomicNoteEntity::class,
        parentColumns = ["note_id"],
        childColumns = ["note_id"],
        onDelete = ForeignKey.Companion.CASCADE
    )],
    indices = [Index("book_id"), Index("note_id")]
)
class TextNoteEntity {
    // Getters and Setters
    @PrimaryKey
    @ColumnInfo(name = "note_id")
    var noteId: Long = 0

    @ColumnInfo(name = "book_id")
    var bookId: Long = 0

    @ColumnInfo(name = "note_text")
    var noteText: String? = null

    @ColumnInfo(name = "created_time_ms")
    var createdTimeMillis: Long = 0

    @ColumnInfo(name = "last_modified_time_ms")
    var lastModifiedTimeMillis: Long = 0

    // No-args constructor required by Room
    constructor()

    constructor(noteId: Long, bookId: Long) {
        this.noteId = noteId
        this.bookId = bookId
        noteText = ""

        createdTimeMillis = System.currentTimeMillis()
        lastModifiedTimeMillis = System.currentTimeMillis()
    }
}



