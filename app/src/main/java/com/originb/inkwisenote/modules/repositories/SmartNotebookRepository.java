package com.originb.inkwisenote.modules.repositories;

import com.originb.inkwisenote.common.DateTimeUtils;
import com.originb.inkwisenote.common.ListUtils;
import com.originb.inkwisenote.modules.smartnotes.data.*;
import com.originb.inkwisenote.common.Strings;

import java.util.*;
import java.util.stream.Collectors;

public class SmartNotebookRepository {

    private final AtomicNoteEntitiesDao atomicNoteEntitiesDao;
    private final SmartBooksDao smartBooksDao;
    private final SmartBookPagesDao smartBookPagesDao;

    public SmartNotebookRepository() {

        this.atomicNoteEntitiesDao = Repositories.getInstance().getNotesDb().atomicNoteEntitiesDao();
        this.smartBooksDao = Repositories.getInstance().getNotesDb().smartBooksDao();
        this.smartBookPagesDao = Repositories.getInstance().getNotesDb().smartBookPagesDao();
    }

    // Create the data and return the notebook entity
    public Optional<SmartNotebook> initializeNewSmartNotebook(String title,
                                                              String directoryPath,
                                                              NoteType noteType) {

        AtomicNoteEntity atomicNoteEntity = newAtomicNote("", directoryPath, noteType);
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

    public void deleteNoteFromBook(SmartNotebook smartNotebook, AtomicNoteEntity atomicNote) {
        // will pages allow this to be deleted first?
        atomicNoteEntitiesDao.deleteAtomicNote(atomicNote.getNoteId());
        smartBookPagesDao.deleteNotePages(atomicNote.getNoteId());

        getSmartNotebooks(smartNotebook.getSmartBook().getBookId())
                .filter(updatedSmartNotebook -> updatedSmartNotebook.atomicNotes.isEmpty())
                .ifPresent(updatedSmartNotebook -> {
                    smartBookPagesDao.deleteSmartBookPages(smartNotebook.smartBook.getBookId());
                    smartBooksDao.deleteSmartBook(smartNotebook.getSmartBook().getBookId());
                });

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

        return getSmartNotebooks(bookId);
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

    public Set<SmartNotebook> getSmartNotebooksForNoteIds(Set<Long> noteIds) {
        List<SmartBookPage> smartBookPages = smartBookPagesDao.getSmartBookPagesOfNote(noteIds);
        Map<Long, List<SmartBookPage>> bookIdToPageMap = ListUtils.groupBy(smartBookPages, SmartBookPage::getBookId);

        List<SmartBookEntity> smartBooks = smartBooksDao.getSmartBooks(bookIdToPageMap.keySet());
        if (smartBooks == null || smartBooks.isEmpty()) return new HashSet<>();

        List<AtomicNoteEntity> atomicNotes = atomicNoteEntitiesDao.getAtomicNotes(noteIds);
        Map<Long, List<AtomicNoteEntity>> noteIdToNotes = ListUtils.groupBy(atomicNotes, AtomicNoteEntity::getNoteId);

        Set<Long> bookIdsToRemove = new HashSet<>();
        Set<SmartNotebook> smartNotebooks = new HashSet<>();
        for (SmartBookEntity smartBook : smartBooks) {
            if (!bookIdToPageMap.containsKey(smartBook.getBookId())) {
                bookIdsToRemove.add(smartBook.getBookId());
                continue;
            }
            List<SmartBookPage> bookPages = bookIdToPageMap.get(smartBook.getBookId());
            List<AtomicNoteEntity> bookNotes = bookPages.stream().map(SmartBookPage::getNoteId)
                    .map(noteIdToNotes::get).filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            smartNotebooks.add(new SmartNotebook(smartBook, smartBookPages, bookNotes));
        }

        return smartNotebooks;
    }

    public Optional<SmartNotebook> getSmartNotebooks(long bookId) {
        SmartBookEntity smartBook = smartBooksDao.getSmartbooksWithMatchingTitle(bookId);
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

    public Set<SmartNotebook> getSmartNotebooks(String title) {
        if (title.length() < 3) return new HashSet<>();

        List<SmartBookEntity> smartBooks = smartBooksDao.getSmartbooksWithMatchingTitle("%" + title + "%");
        if (smartBooks == null || smartBooks.isEmpty()) return new HashSet<>();

        Set<Long> bookIds = smartBooks.stream().map(SmartBookEntity::getBookId).collect(Collectors.toSet());
        List<SmartBookPage> smartBookPages = smartBookPagesDao.getSmartBooksPages(bookIds);
        Map<Long, List<SmartBookPage>> bookIdToPagesMap = ListUtils.groupBy(smartBookPages, SmartBookPage::getBookId);

        Set<Long> noteIds = smartBookPages.stream().map(SmartBookPage::getNoteId).collect(Collectors.toSet());
        List<AtomicNoteEntity> atomicNotes = atomicNoteEntitiesDao.getAtomicNotes(noteIds);
        Map<Long, List<AtomicNoteEntity>> noteIdToNotes = ListUtils.groupBy(atomicNotes, AtomicNoteEntity::getNoteId);

        Set<SmartNotebook> smartNotebooks = new HashSet<>();
        Set<Long> bookIdsOfEmptyBooks = new HashSet<>();
        for (SmartBookEntity smartBook : smartBooks) {
            long bookId = smartBook.getBookId();
            if (!bookIdToPagesMap.containsKey(bookId)) {
                bookIdsOfEmptyBooks.add(bookId);
                continue;
            }
            List<SmartBookPage> bookPages = bookIdToPagesMap.get(bookId);
            List<AtomicNoteEntity> bookNotes = bookPages.stream().map(SmartBookPage::getNoteId)
                    .map(noteIdToNotes::get).filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            smartNotebooks.add(new SmartNotebook(smartBook, bookPages, bookNotes));
        }

        return smartNotebooks;
    }

    public AtomicNoteEntity newAtomicNote(String filename, String filepath, NoteType noteType) {
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



























