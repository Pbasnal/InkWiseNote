package com.originb.inkwisenote.modules.repositories;

import com.originb.inkwisenote.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote.modules.smartnotes.data.SmartBookEntity;
import com.originb.inkwisenote.modules.smartnotes.data.SmartBookPage;
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

    public void insertAtomicNoteAndPage(int position, AtomicNoteEntity atomicNote, SmartBookPage newPage) {
        atomicNotes.add(position, atomicNote);
        smartBookPages.add(position, newPage);
        int pageOrder = 0;
        for (SmartBookPage smartBookPage : smartBookPages) {
            smartBookPage.setPageOrder(pageOrder);
            pageOrder++;
        }
    }

    public void removeNote(long noteId) {
        smartBookPages.removeIf(p -> p.getNoteId() == noteId);
        atomicNotes.removeIf(p -> p.getNoteId() == noteId);
    }
}
