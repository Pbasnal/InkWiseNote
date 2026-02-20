package com.originb.inkwisenote2.modules.repositories

import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity

class NoteRelationRepository(
    private val noteRelationDao: NoteRelationDao,
    private val noteOcrTextDao: NoteOcrTextsDao,
    private val noteTermFrequencyDao: NoteTermFrequencyDao
) {
    fun deleteNoteRelationData(atomicNote: AtomicNoteEntity) {
        noteRelationDao.deleteByNoteId(atomicNote.noteId)
        noteOcrTextDao.deleteNoteText(atomicNote.noteId)
        noteTermFrequencyDao.deleteTermFrequencies(atomicNote.noteId)
    }
}
