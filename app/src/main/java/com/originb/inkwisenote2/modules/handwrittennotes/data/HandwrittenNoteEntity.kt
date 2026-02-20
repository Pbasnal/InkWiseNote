package com.originb.inkwisenote2.modules.handwrittennotes.data

import androidx.room.*
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity

@Entity(
    tableName = "handwritten_notes",
    foreignKeys = [ForeignKey(
        entity = SmartBookEntity::class,
        parentColumns = "book_id",
        childColumns = "book_id",
        onDelete = ForeignKey.Companion.CASCADE
    ), ForeignKey(
        entity = AtomicNoteEntity::class,
        parentColumns = "note_id",
        childColumns = "note_id",
        onDelete = ForeignKey.Companion.CASCADE
    )],
    indices = [Index("book_id"), Index("note_id")]
)
class HandwrittenNoteEntity  // No-args constructor required by Room
{
    // Getters and Setters
    @PrimaryKey
    @ColumnInfo(name = "note_id")
    var noteId: Long = 0

    @ColumnInfo(name = "book_id")
    var bookId: Long = 0

    @ColumnInfo(name = "bitmap_file_path")
    var bitmapFilePath: String? = null

    @ColumnInfo(name = "bitmap_hash")
    var bitmapHash: String? = null

    @ColumnInfo(name = "page_template_file_path")
    var pageTemplateFilePath: String? = null

    @ColumnInfo(name = "page_template_hash")
    var pageTemplateHash: String? = null

    @ColumnInfo(name = "created_time_ms")
    var createdTimeMillis: Long = 0

    @ColumnInfo(name = "last_modified_time_ms")
    var lastModifiedTimeMillis: Long = 0
} // todo: Add width and height of the drawingView
// then use logical space to create the canvas
// check notion for details

