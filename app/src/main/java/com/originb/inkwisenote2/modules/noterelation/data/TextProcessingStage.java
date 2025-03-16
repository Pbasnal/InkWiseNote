package com.originb.inkwisenote2.modules.noterelation.data;

public enum TextProcessingStage {
    TEXT_PARSING("text_parsing"),
    TOKENIZATION("tokenization"),
    NOTE_READY("note_ready");

    private final String textProcessingStage;

    TextProcessingStage(String textProcessingStage) {
        this.textProcessingStage = textProcessingStage;
    }

    @Override
    public String toString() {
        return textProcessingStage;
    }

    public boolean isEqualTo(String stage) {
        return this.textProcessingStage.equals(stage);
    }
}