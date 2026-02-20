package com.originb.inkwisenote2.modules.ocr.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "note_term_frequency", indices = [Index("note_id")])
class NoteTermFrequency {
    // Getters and Setters
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @JvmField
    @ColumnInfo(name = "note_id")
    var noteId: Long = 0

    @JvmField
    @ColumnInfo(name = "term")
    var term: String? = null

    @JvmField
    @ColumnInfo(name = "fq_in_doc")
    var termFrequency: Int = 0

    // No-args constructor required by Room
    constructor()

    constructor(noteId: Long, term: String?, termFrequency: Int) {
        this.noteId = noteId
        this.term = term
        this.termFrequency = termFrequency
    }
}


