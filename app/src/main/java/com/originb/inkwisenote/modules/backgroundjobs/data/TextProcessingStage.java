package com.originb.inkwisenote.modules.backgroundjobs.data;

public enum TextProcessingStage {
    TEXT_PARSING("text_parsing"),
    TOKENIZATION("tokenization");

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