package com.originb.inkwisenote.data.entities.tasks;

public enum NoteTaskStage {
    TEXT_PARSING("TEXT_PARSING"),
    TOKENIZATION("TOKENIZATION"),
    NOTE_READY("NOTE_READY");

    private final String textProcessingStage;

    NoteTaskStage(String textProcessingStage) {
        this.textProcessingStage = textProcessingStage;
    }

    @Override
    public String toString() {
        return textProcessingStage;
    }

    public boolean equals(NoteTaskStage noteTaskStage) {
        return this.toString().equals(noteTaskStage.toString());
    }
}