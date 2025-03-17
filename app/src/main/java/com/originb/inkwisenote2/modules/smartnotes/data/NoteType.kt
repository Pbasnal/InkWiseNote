package com.originb.inkwisenote2.modules.smartnotes.data

enum class NoteType(private val noteType: String) {
    HANDWRITTEN_PNG("handwritten_png"),
    TEXT_NOTE("text_note"),
    NOT_SET("not_set");

    override fun toString(): String {
        return noteType
    }

    fun equals(noteType: String): Boolean {
        return this.noteType == noteType
    }
}
