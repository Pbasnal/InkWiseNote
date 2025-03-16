package com.originb.inkwisenote2.modules.admin;

public class NoteExtractedTextEntry {
    private final Long noteId;
    private final String text;

    public NoteExtractedTextEntry(Long noteId, String text) {
        this.noteId = noteId;
        this.text = text;
    }

    public Long getNoteId() {
        return noteId;
    }

    public String getText() {
        return text;
    }
} 