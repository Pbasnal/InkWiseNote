package com.originb.inkwisenote.data.notedata;

import com.originb.inkwisenote.io.ocr.AzureOcrResult;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class NoteMeta implements Serializable {
    private Long noteId;
    private String noteFileName;
    private String noteTitle;
    private Long createdTimeMillis;
    private Long lastModifiedTimeMillis;
    private Set<Long> prevNoteIds;
    private Set<Long> nextNoteIds;

    private AzureOcrResult azureOcrResult;
    private String extractedText;

    public NoteMeta(Long noteId) {
        this.noteId = noteId;
        this.prevNoteIds = new HashSet<>();
        this.nextNoteIds = new HashSet<>();
    }

    public String getCreateDateTimeString() {
        Instant instant = Instant.ofEpochMilli(createdTimeMillis);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        return formatter.format(instant);
    }
}