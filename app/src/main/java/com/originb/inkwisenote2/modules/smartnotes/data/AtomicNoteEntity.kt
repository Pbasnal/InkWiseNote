package com.originb.inkwisenote2.modules.smartnotes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "atomic_note_entities")
class AtomicNoteEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    private val noteId: Long = 0

    @ColumnInfo(name = "filename")
    private val filename: String? = null

    @ColumnInfo(name = "filepath")
    private val filepath: String? = null

    @ColumnInfo(name = "note_type")
    private val noteType: String? = null // for handwritten and text

    @ColumnInfo(name = "page_template_id")
    private val pageTemplateId: Long = 0

    @ColumnInfo(name = "created_time_ms")
    private val createdTimeMillis: Long = 0

    @ColumnInfo(name = "last_modified_time_ms")
    private val lastModifiedTimeMillis: Long = 0
}




















