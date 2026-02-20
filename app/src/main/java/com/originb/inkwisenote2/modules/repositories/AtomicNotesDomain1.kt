package com.originb.inkwisenote2.modules.repositories

import com.originb.inkwisenote2.common.Strings.isNullOrWhitespace
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntitiesDao
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType

class AtomicNotesDomain(private val atomicNoteEntitiesDao: AtomicNoteEntitiesDao) {
    fun saveAtomicNote(atomicNoteEntity: AtomicNoteEntity): AtomicNoteEntity {
        val noteId = atomicNoteEntitiesDao.insertAtomicNote(atomicNoteEntity)
        atomicNoteEntity.noteId = noteId

        return atomicNoteEntity
    }

    fun updateAtomicNotes(atomicNotes: MutableList<AtomicNoteEntity?>?): Int {
        val numberOfUpdatedNotes = atomicNoteEntitiesDao.updateAtomicNotes(atomicNotes)
        return numberOfUpdatedNotes
    }

    fun updateAtomicNote(atomicNote: AtomicNoteEntity?): Int {
        val numberOfUpdatedNotes = atomicNoteEntitiesDao.updateAtomicNote(atomicNote)
        return numberOfUpdatedNotes
    }

    fun getAtomicNotes(noteIds: MutableSet<Long?>?): MutableList<AtomicNoteEntity?>? {
        return atomicNoteEntitiesDao.getAtomicNotes(noteIds)
    }

    fun getAtomicNote(noteId: Long): AtomicNoteEntity? {
        return atomicNoteEntitiesDao.getAtomicNote(noteId)
    }

    companion object {
        @JvmStatic
        fun constructAtomicNote(filename: String?, filepath: String?, noteType: NoteType): AtomicNoteEntity {
            val createdTimeMillis = System.currentTimeMillis()
            val atomicNoteEntity = AtomicNoteEntity()
            atomicNoteEntity.createdTimeMillis = createdTimeMillis

            if (isNullOrWhitespace(filename)) {
                atomicNoteEntity.filename = createdTimeMillis.toString()
            } else {
                atomicNoteEntity.filename = filename
            }
            if (isNullOrWhitespace(filepath)) {
                atomicNoteEntity.filepath = ""
            } else {
                atomicNoteEntity.filepath = filepath
            }

            atomicNoteEntity.noteType = noteType.toString()

            return atomicNoteEntity
        }
    }
}
