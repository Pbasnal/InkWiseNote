package com.originb.inkwisenote2.modules.smarthome

import android.graphics.Bitmap
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import lombok.Getter
import lombok.Setter

@Getter
@Setter
class QueryNoteResult(atomicNoteEntity: AtomicNoteEntity) {
    private val noteId: Long
    private val noteImage: Bitmap? = null
    private val queryWord: String? = null
    private val noteText: String? = null
    private var lastModifiedMillis: Long = 0
    private val noteType: NoteType? = null

    init {
        noteId = atomicNoteEntity.noteId
        if (atomicNoteEntity.lastModifiedTimeMillis != 0L) {
            lastModifiedMillis = atomicNoteEntity.lastModifiedTimeMillis
        } else {
            lastModifiedMillis = atomicNoteEntity.createdTimeMillis
        }
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null || javaClass != obj.javaClass) return false

        val note = obj as QueryNoteResult

        // Custom equality logic (e.g., comparing titles only)
        return noteId == note.noteId
    }

    override fun hashCode(): Int {
        // Use the same attributes as equals() to generate hashCode
        return noteId.hashCode()
    }
}
