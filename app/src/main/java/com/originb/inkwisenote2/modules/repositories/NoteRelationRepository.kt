package com.originb.inkwisenote2.modules.repositories

import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity

class NoteRelationRepository {
    private val noteRelationDao: NoteRelationDao =
        Repositories.Companion.getInstance().getNotesDb().noteRelationDao()
    private val noteOcrTextDao: NoteOcrTextDao =
        Repositories.Companion.getInstance().getNotesDb().noteOcrTextDao()
    private val noteTermFrequencyDao: NoteTermFrequencyDao =
        Repositories.Companion.getInstance().getNotesDb().noteTermFrequencyDao()

    fun deleteNoteRelationData(atomicNote: AtomicNoteEntity?) {
        noteRelationDao.deleteByNoteId(atomicNote.getNoteId())
        noteOcrTextDao.deleteNoteText(atomicNote.getNoteId())
        noteTermFrequencyDao.deleteTermFrequencies(atomicNote.getNoteId())
    }
}
