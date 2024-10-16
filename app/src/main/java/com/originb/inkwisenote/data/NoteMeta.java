package com.originb.inkwisenote.data;

import com.originb.inkwisenote.io.ocr.AzureOcrResult;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
}