package com.originb.inkwisenote2.modules.smartnotes.data

import androidx.room.*

@Entity(
    tableName = "smart_book_pages",
    foreignKeys = [ForeignKey(
        entity = SmartBookEntity::class,
        parentColumns = ["book_id"],
        childColumns = ["book_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = AtomicNoteEntity::class,
        parentColumns = ["note_id"],
        childColumns = ["note_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("book_id"), Index("note_id")]
)
class SmartBookPage {
    // Getters and Setters
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "book_id")
    var bookId: Long = 0

    @ColumnInfo(name = "note_id")
    var noteId: Long = 0

    @ColumnInfo(name = "page_order")
    var pageOrder: Int = 0

    // No-args constructor required by Room
    constructor()

    constructor(bookId: Long, noteId: Long, pageOrder: Int) {
        this.bookId = bookId
        this.noteId = noteId
        this.pageOrder = pageOrder
    }
}
