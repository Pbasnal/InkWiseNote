package com.originb.inkwisenote.modules.repositories;

import com.originb.inkwisenote.commonutils.DateTimeUtils;
import com.originb.inkwisenote.data.dao.notes.AtomicNoteEntitiesDao;
import com.originb.inkwisenote.data.dao.notes.SmartBookPagesDao;
import com.originb.inkwisenote.data.dao.notes.SmartBooksDao;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookPage;
import com.originb.inkwisenote.modules.commonutils.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SmartNotebookRepository {

    //    private NoteMetaFiles noteMetaFiles;

//    private NoteOcrTextDao noteOcrTextDao;
//    private NoteTermFrequencyDao noteTermFrequencyDao;
//
//    private NoteRelationDao noteRelationDao;

    private final AtomicNoteEntitiesDao atomicNoteEntitiesDao;
    private final SmartBooksDao smartBooksDao;
    private final SmartBookPagesDao smartBookPagesDao;

    public SmartNotebookRepository() {
//        this.noteMetaFiles = Repositories.getInstance().getNoteMetaRepository();

//        this.noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();
//        this.noteTermFrequencyDao = Repositories.getInstance().getNotesDb().noteTermFrequencyDao();
//        this.noteRelationDao = Repositories.getInstance().getNotesDb().noteRelationDao();

        this.atomicNoteEntitiesDao = Repositories.getInstance().getNotesDb().atomicNoteEntitiesDao();
        this.smartBooksDao = Repositories.getInstance().getNotesDb().smartBooksDao();
        this.smartBookPagesDao = Repositories.getInstance().getNotesDb().smartBookPagesDao();
    }


    // Create the data and return the notebook entity
    public Optional<SmartNotebook> initializeNewSmartNotebook(String title,
                                                              String directoryPath) {

        AtomicNoteEntity atomicNoteEntity = newHandwrittenNote("", directoryPath);

        SmartBookEntity smartBookEntity = newSmartBook(title, atomicNoteEntity.getCreatedTimeMillis());

        SmartBookPage smartBookPage = newSmartBookPage(smartBookEntity, atomicNoteEntity, 0);

        SmartNotebook smartNotebook = new SmartNotebook(smartBookEntity, smartBookPage, atomicNoteEntity);
        return Optional.ofNullable(smartNotebook);
    }

    public void deleteSmartNotebook(SmartNotebook smartNotebook) {
        // will pages allow this to be deleted first?
        smartNotebook.getAtomicNotes()
                .stream().map(AtomicNoteEntity::getNoteId)
                .forEach(atomicNoteEntitiesDao::deleteAtomicNote);

        smartBookPagesDao.deleteSmartBookPages(smartNotebook.getSmartBook().getBookId());

        smartBooksDao.deleteSmartBook(smartNotebook.getSmartBook().getBookId());
    }

    public void updateNotebook(SmartNotebook smartNotebook) {
        long updateTime = System.currentTimeMillis();

        SmartBookEntity smartBookEntity = smartNotebook.getSmartBook();
        smartBookEntity.setLastModifiedTimeMillis(System.currentTimeMillis());
        smartBooksDao.updateSmartBook(smartBookEntity);

        for (AtomicNoteEntity atomicNote : smartNotebook.getAtomicNotes()) {
            atomicNote.setLastModifiedTimeMillis(updateTime);
        }
        atomicNoteEntitiesDao.updateAtomicNote(smartNotebook.getAtomicNotes());

        int updateResult = smartBookPagesDao.updateSmartBookPage(smartNotebook.getSmartBookPages());
    }

    public Optional<SmartNotebook> getSmartNotebookContainingNote(long noteId) {
        List<SmartBookPage> pagesOfNote = smartBookPagesDao.getSmartBookPagesOfNote(noteId);
        if (pagesOfNote == null || pagesOfNote.isEmpty()) return Optional.empty();

        // TODO: only get the first page for now. We will fetch more later
        long bookId = pagesOfNote.stream().findFirst().map(SmartBookPage::getBookId).get();

        return getSmartNotebook(bookId);
    }

    public List<SmartNotebook> getAllSmartNotebooks() {
        List<SmartBookEntity> smartBooks = smartBooksDao.getAllSmartBooks();
        if (smartBooks == null || smartBooks.isEmpty()) return new ArrayList<>();

        List<SmartNotebook> smartNotebooks = new ArrayList<>();

        for (SmartBookEntity smartBook : smartBooks) {
            List<SmartBookPage> smartBookPages = smartBookPagesDao.getSmartBookPages(smartBook.getBookId());
            if (smartBookPages == null || smartBookPages.isEmpty()) continue;

            Set<Long> noteIds = smartBookPages.stream()
                    .map(SmartBookPage::getNoteId)
                    .collect(Collectors.toSet());

            List<AtomicNoteEntity> atomicNoteEntities = atomicNoteEntitiesDao.getAtomicNotes(noteIds);

            smartNotebooks.add(new SmartNotebook(smartBook, smartBookPages, atomicNoteEntities));
        }

        return smartNotebooks;
    }

    public Optional<SmartNotebook> getSmartNotebook(long bookId) {
        SmartBookEntity smartBook = smartBooksDao.getSmartBook(bookId);
        if (smartBook == null) return Optional.empty();

        List<SmartBookPage> smartBookPages = smartBookPagesDao.getSmartBookPages(bookId);
        if (smartBookPages == null || smartBookPages.isEmpty()) return Optional.empty();

        Set<Long> noteIds = smartBookPages.stream()
                .map(SmartBookPage::getNoteId)
                .collect(Collectors.toSet());

        List<AtomicNoteEntity> atomicNoteEntities = atomicNoteEntitiesDao.getAtomicNotes(noteIds);

        SmartNotebook smartNotebook = new SmartNotebook(smartBook, smartBookPages, atomicNoteEntities);

        return Optional.ofNullable(smartNotebook);
    }

    public AtomicNoteEntity newHandwrittenNote(String filename, String filepath) {
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

        atomicNoteEntity.setNoteType("handwritten_png");

        long noteId = atomicNoteEntitiesDao.insertAtomicNote(atomicNoteEntity);
        atomicNoteEntity.setNoteId(noteId);

        return atomicNoteEntity;
    }

    private SmartBookEntity newSmartBook(String title, long createdDateTimeMs) {
        SmartBookEntity smartBookEntity = new SmartBookEntity();
        if (Strings.isNullOrWhitespace(title)) {
            smartBookEntity.setTitle(DateTimeUtils.msToDateTime(createdDateTimeMs));
        } else {
            smartBookEntity.setTitle(title);
        }
        smartBookEntity.setCreatedTimeMillis(createdDateTimeMs);
        smartBookEntity.setLastModifiedTimeMillis(createdDateTimeMs);

        long bookId = smartBooksDao.insertSmartBook(smartBookEntity);
        smartBookEntity.setBookId(bookId);

        return smartBookEntity;
    }

    public SmartBookPage newSmartBookPage(SmartBookEntity smartBookEntity,
                                          AtomicNoteEntity atomicNoteEntity,
                                          int pageOrder) {

        SmartBookPage smartBookPage = new SmartBookPage(smartBookEntity.getBookId(),
                atomicNoteEntity.getNoteId(),
                pageOrder);
        long id = smartBookPagesDao.insertSmartBookPage(smartBookPage);
        smartBookPage.setId(id);

        return smartBookPage;
    }


}



























