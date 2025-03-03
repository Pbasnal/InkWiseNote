package com.originb.inkwisenote.modules.ocr.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(tableName = "note_term_frequency")
public class NoteTermFrequency {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "note_id")
    private long noteId;

    @ColumnInfo(name = "term")
    private String term;

    @ColumnInfo(name = "fq_in_doc")
    private int termFrequency;

    public NoteTermFrequency(long noteId, String term, int termFrequency) {
        this.noteId = noteId;
        this.term = term;
        this.termFrequency = termFrequency;
    }
}


