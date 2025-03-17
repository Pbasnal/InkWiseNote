package com.originb.inkwisenote2.modules.noterelation.data

enum class TextProcessingStage(private val textProcessingStage: String) {
    TEXT_PARSING("text_parsing"),
    TOKENIZATION("tokenization"),
    NOTE_READY("note_ready");

    override fun toString(): String {
        return textProcessingStage
    }

    fun isEqualTo(stage: String): Boolean {
        return this.textProcessingStage == stage
    }
}