package com.originb.inkwisenote.modules.repositories;

import com.originb.inkwisenote.data.dao.noteocr.NoteOcrTextDao;
import com.originb.inkwisenote.data.dao.noteocr.NoteTermFrequencyDao;
import com.originb.inkwisenote.data.dao.noterelation.NoteRelationDao;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;

public class NoteRelationRepository {
    private final NoteRelationDao noteRelationDao;
    private final NoteOcrTextDao noteOcrTextDao;
    private final NoteTermFrequencyDao noteTermFrequencyDao;

    public NoteRelationRepository() {
        noteRelationDao = Repositories.getInstance().getNotesDb().noteRelationDao();
        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();
        noteTermFrequencyDao = Repositories.getInstance().getNotesDb().noteTermFrequencyDao();
    }

    public void deleteNoteRelationData(AtomicNoteEntity atomicNote) {
        noteRelationDao.deleteByNoteId(atomicNote.getNoteId());
        noteOcrTextDao.deleteNoteText(atomicNote.getNoteId());
        noteTermFrequencyDao.deleteTermFrequencies(atomicNote.getNoteId());
    }
}
