package com.originb.inkwisenote2.modules.smartnotes.data;

public enum NoteType {
    HANDWRITTEN_PNG("handwritten_png"),
    TEXT_NOTE("text_note"),
    NOT_SET("not_set");

    private final String noteType;

    NoteType(String noteType) {
        this.noteType = noteType;
    }

    @Override
    public String toString() {
        return noteType;
    }

    public boolean equals(String noteType) {
        return this.noteType.equals(noteType);
    }
}
