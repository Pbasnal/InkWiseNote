package com.originb.inkwisenote2.modules.smartnotes.data

import androidx.room.*
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Getter
@Setter
@NoArgsConstructor
@Entity(
    tableName = "smart_book_pages",
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
class SmartBookPage(
    @field:ColumnInfo(name = "book_id") private val bookId: Long,
    @field:ColumnInfo(name = "note_id") private val noteId: Long,
    @field:ColumnInfo(
        name = "page_order"
    ) private val pageOrder: Int
) {
    @PrimaryKey(autoGenerate = true)
    private val id: Long = 0
}
