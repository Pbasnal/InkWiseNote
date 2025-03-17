package com.originb.inkwisenote2.modules.noterelation.data

import androidx.room.*
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity
import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
@Entity(
    tableName = "note_relation",
    foreignKeys = [ForeignKey(
        entity = AtomicNoteEntity::class,
        parentColumns = "note_id",
        childColumns = "note_id",
        onDelete = CASCADE
    ), ForeignKey(
        entity = SmartBookEntity::class,
        parentColumns = "book_id",
        childColumns = "book_id",
        onDelete = CASCADE
    ), ForeignKey(
        entity = AtomicNoteEntity::class,
        parentColumns = "note_id",
        childColumns = "related_note_id",
        onDelete = CASCADE
    ), ForeignKey(
        entity = SmartBookEntity::class,
        parentColumns = "book_id",
        childColumns = "related_book_id",
        onDelete = CASCADE
    )]
)
class NoteRelation(
    @field:ColumnInfo(name = "note_id") private val noteId: Long,
    @field:ColumnInfo(name = "related_note_id") private val relatedNoteId: Long,
    @field:ColumnInfo(
        name = "book_id"
    ) private val bookId: Long,
    @field:ColumnInfo(name = "related_book_id") private val relatedBookId: Long?,
    @field:ColumnInfo(
        name = "relation_type"
    ) private val relationType: Int
) {
    @PrimaryKey(autoGenerate = true)
    private val id = 0

    // Custom equals that compares only 'id'
    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true // Reference equality

        if (obj == null || javaClass != obj.javaClass) return false // Type check

        val that = obj as NoteRelation
        return noteId == that.noteId && relatedNoteId == that.relatedNoteId && bookId == that.bookId && relatedBookId == that.relatedBookId
    }

    // Custom hashCode that considers only 'id'
    override fun hashCode(): Int {
        return Objects.hash(noteId, relatedNoteId, bookId, relatedBookId) // Hash based on 'id'
    }
}
