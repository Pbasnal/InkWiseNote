package com.originb.inkwisenote2.modules.repositories;

import com.originb.inkwisenote2.common.Strings;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntitiesDao;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;

import java.util.List;
import java.util.Set;

public class AtomicNotesDomain {
    private final AtomicNoteEntitiesDao atomicNoteEntitiesDao;

    public AtomicNotesDomain() {
        this.atomicNoteEntitiesDao = Repositories.getInstance().getNotesDb().atomicNoteEntitiesDao();
    }

    public static AtomicNoteEntity constructAtomicNote(String filename, String filepath, NoteType noteType) {
        long createdTimeMillis = System.currentTimeMillis();
        AtomicNoteEntity atomicNoteEntity = new AtomicNoteEntity();
        atomicNoteEntity.setCreatedTimeMillis(createdTimeMillis);

        if (Strings.isNullOrWhitespace(filename)) {
            atomicNoteEntity.setFilename(String.valueOf(createdTimeMillis));
        } else {
            atomicNoteEntity.setFilename(filename);
        }
        if (Strings.isNullOrWhitespace(filepath)) {
            atomicNoteEntity.setFilepath("");
        } else {
            atomicNoteEntity.setFilepath(filepath);
        }

        atomicNoteEntity.setNoteType(noteType.toString());

        return atomicNoteEntity;
    }

    public AtomicNoteEntity saveAtomicNote(AtomicNoteEntity atomicNoteEntity) {
        long noteId = atomicNoteEntitiesDao.insertAtomicNote(atomicNoteEntity);
        atomicNoteEntity.setNoteId(noteId);

        return atomicNoteEntity;
    }

    public int updateAtomicNotes(List<AtomicNoteEntity> atomicNotes) {
        int numberOfUpdatedNotes = atomicNoteEntitiesDao.updateAtomicNotes(atomicNotes);
        return numberOfUpdatedNotes;
    }

    public int updateAtomicNote(AtomicNoteEntity atomicNote) {
        int numberOfUpdatedNotes = atomicNoteEntitiesDao.updateAtomicNote(atomicNote);
        return numberOfUpdatedNotes;
    }

    public List<AtomicNoteEntity> getAtomicNotes(Set<Long> noteIds) {
        return atomicNoteEntitiesDao.getAtomicNotes(noteIds);
    }

    public AtomicNoteEntity getAtomicNote(long noteId) {
        return atomicNoteEntitiesDao.getAtomicNote(noteId);
    }
}
