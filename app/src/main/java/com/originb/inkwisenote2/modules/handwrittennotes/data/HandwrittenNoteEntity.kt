package com.originb.inkwisenote2.modules.handwrittennotes.data

import androidx.room.*
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Getter
@Setter
@NoArgsConstructor
@Entity(
    tableName = "handwritten_notes",
    foreignKeys = [ForeignKey(
        entity = SmartBookEntity::class,
        parentColumns = "book_id",
        childColumns = "book_id",
        onDelete = CASCADE
    ), ForeignKey(
        entity = AtomicNoteEntity::class,
        parentColumns = "note_id",
        childColumns = "note_id",
        onDelete = CASCADE
    )]
)
class HandwrittenNoteEntity {
    @PrimaryKey
    @ColumnInfo(name = "note_id")
    private val noteId: Long = 0

    @ColumnInfo(name = "book_id")
    private val bookId: Long = 0

    @ColumnInfo(name = "bitmap_file_path")
    private val bitmapFilePath: String? = null

    @ColumnInfo(name = "bitmap_hash")
    private val bitmapHash: String? = null

    @ColumnInfo(name = "page_template_file_path")
    private val pageTemplateFilePath: String? = null

    @ColumnInfo(name = "page_template_hash")
    private val pageTemplateHash: String? = null

    @ColumnInfo(name = "created_time_ms")
    private val createdTimeMillis: Long = 0

    @ColumnInfo(name = "last_modified_time_ms")
    private val lastModifiedTimeMillis: Long = 0
}




















