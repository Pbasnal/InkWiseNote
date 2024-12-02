package com.originb.inkwisenote.data.notedata;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class NoteOcrText implements Serializable {
    private Long noteId;
    private Long createdTimeMillis;
    private Long lastModifiedTimeMillis;
    private String extractedText;

    public NoteOcrText(Long noteId, String text) {
        this.noteId = noteId;
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
