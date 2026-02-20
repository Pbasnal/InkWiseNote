package com.originb.inkwisenote2.modules.noterelation.data

import androidx.room.*
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity
import java.util.*

@Entity(
    tableName = "note_relation",
    foreignKeys = [ForeignKey(
        entity = AtomicNoteEntity::class,
        parentColumns = "note_id",
        childColumns = "note_id",
        onDelete = ForeignKey.Companion.CASCADE
    ), ForeignKey(
        entity = SmartBookEntity::class,
        parentColumns = "book_id",
        childColumns = "book_id",
        onDelete = ForeignKey.Companion.CASCADE
    ), ForeignKey(
        entity = AtomicNoteEntity::class,
        parentColumns = "note_id",
        childColumns = "related_note_id",
        onDelete = ForeignKey.Companion.CASCADE
    ), ForeignKey(
        entity = SmartBookEntity::class,
        parentColumns = "book_id",
        childColumns = "related_book_id",
        onDelete = ForeignKey.Companion.CASCADE
    )],
    indices = [Index("note_id"), Index("book_id"), Index("related_note_id"), Index("related_book_id")]
)
class NoteRelation(
    @JvmField @field:ColumnInfo(name = "note_id") var noteId: Long,
    @JvmField @field:ColumnInfo(name = "related_note_id") var relatedNoteId: Long,
    @JvmField @field:ColumnInfo(
        name = "book_id"
    ) var bookId: Long,
    @JvmField @field:ColumnInfo(name = "related_book_id") var relatedBookId: Long,
    @JvmField @field:ColumnInfo(
        name = "relation_type"
    ) var relationType: Int
) {
    // Getters and Setters
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    // Custom equals that compares only 'id'
    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true // Reference equality

        if (obj == null || javaClass != obj.javaClass) return false // Type check

        val that = obj as NoteRelation
        return noteId == that.noteId
                && relatedNoteId == that.relatedNoteId
                && bookId == that.bookId
                && relatedBookId == that.relatedBookId
    }

    // Custom hashCode that considers only 'id'
    override fun hashCode(): Int {
        return Objects.hash(noteId, relatedNoteId, bookId, relatedBookId) // Hash based on 'id'
    }
}
