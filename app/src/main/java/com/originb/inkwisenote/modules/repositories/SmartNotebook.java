package com.originb.inkwisenote.modules.repositories;

import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookPage;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SmartNotebook {
    public SmartBookEntity smartBook;
    public List<SmartBookPage> smartBookPages;
    public List<AtomicNoteEntity> atomicNotes;

    public SmartNotebook(SmartBookEntity smartBook,
                         SmartBookPage smartBookPage,
                         AtomicNoteEntity atomicNoteEntity) {
        this.smartBookPages = new ArrayList<>();
        this.atomicNotes = new ArrayList<>();

        this.smartBook = smartBook;
        smartBookPages.add(smartBookPage);
        atomicNotes.add(atomicNoteEntity);
    }

    public SmartNotebook(SmartBookEntity smartBook,
                         List<SmartBookPage> smartBookPages,
                         List<AtomicNoteEntity> atomicNoteEntities) {
        this.smartBook = smartBook;
        this.smartBookPages = smartBookPages;
        this.atomicNotes = atomicNoteEntities;
    }
}
