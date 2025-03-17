package com.originb.inkwisenote2.modules.textnote.data

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
    tableName = "text_notes",
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
class TextNoteEntity(
    @field:ColumnInfo(name = "note_id") @field:PrimaryKey private val noteId: Long, @field:ColumnInfo(
        name = "book_id"
    ) private val bookId: Long
) {
    @ColumnInfo(name = "note_text")
    private val noteText = ""

    @ColumnInfo(name = "created_time_ms")
    private val createdTimeMillis = System.currentTimeMillis()

    @ColumnInfo(name = "last_modified_time_ms")
    private val lastModifiedTimeMillis = System.currentTimeMillis()
}




















