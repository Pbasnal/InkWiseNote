package com.originb.inkwisenote.data.entities.noteocrdata;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "note_text")
public class NoteOcrText implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "note_id")
    private Long noteId;

    @ColumnInfo(name = "extracted_text")
    private String extractedText;

    @ColumnInfo(name = "note_hash")
    private String noteHash;

    @ColumnInfo(name = "created_time_ms")
    private Long createdTimeMillis;

    @ColumnInfo(name = "last_modified_time_ms")
    private Long lastModifiedTimeMillis;


    public NoteOcrText(Long noteId, String noteHash, String text) {
        this.noteId = noteId;
        this.noteHash = noteHash;
        this.extractedText = text;
        createdTimeMillis = System.currentTimeMillis();
        lastModifiedTimeMillis = System.currentTimeMillis();
    }

    public String getCreateDateTimeString() {
        Instant instant = Instant.ofEpochMilli(createdTimeMillis);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        return formatter.format(instant);
    }
}
