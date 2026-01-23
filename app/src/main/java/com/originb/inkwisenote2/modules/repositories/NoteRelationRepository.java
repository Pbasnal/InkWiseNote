package com.originb.inkwisenote2.modules.repositories;

import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao;
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;

public class NoteRelationRepository {
    private final NoteRelationDao noteRelationDao;
    private final NoteOcrTextDao noteOcrTextDao;
    private final NoteTermFrequencyDao noteTermFrequencyDao;

    public NoteRelationRepository(NoteRelationDao noteRelationDao,
                                  NoteOcrTextDao noteOcrTextDao,
                                  NoteTermFrequencyDao noteTermFrequencyDao) {
        this.noteRelationDao = noteRelationDao;
        this.noteOcrTextDao = noteOcrTextDao;
        this.noteTermFrequencyDao = noteTermFrequencyDao;
    }

    public void deleteNoteRelationData(AtomicNoteEntity atomicNote) {
        noteRelationDao.deleteByNoteId(atomicNote.getNoteId());
        noteOcrTextDao.deleteNoteText(atomicNote.getNoteId());
        noteTermFrequencyDao.deleteTermFrequencies(atomicNote.getNoteId());
    }
}
