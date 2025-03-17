package com.originb.inkwisenote2.modules.ocr.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@Entity(tableName = "note_term_frequency")
class NoteTermFrequency(
    @field:ColumnInfo(name = "note_id") private val noteId: Long,
    @field:ColumnInfo(name = "term") private val term: String?,
    @field:ColumnInfo(
        name = "fq_in_doc"
    ) private val termFrequency: Int
) {
    @PrimaryKey(autoGenerate = true)
    private val id = 0
}


