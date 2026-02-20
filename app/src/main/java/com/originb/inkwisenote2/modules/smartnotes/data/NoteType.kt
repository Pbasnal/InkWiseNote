package com.originb.inkwisenote2.modules.smartnotes.data

enum class NoteType(private val noteType: String) {
    HANDWRITTEN_PNG("handwritten_png"),
    TEXT_NOTE("text_note"),
    NOT_SET("not_set");

    override fun toString(): String {
        return noteType
    }

    fun equals(noteType: String?): Boolean {
        return this.noteType == noteType
    }

    companion object {
        fun fromString(noteType: String): NoteType {
            when (noteType) {
                "handwritten_png" -> return NoteType.HANDWRITTEN_PNG
                "text_note" -> return NoteType.TEXT_NOTE
                else -> return NoteType.NOT_SET
            }
        }
    }
}
