package com.originb.inkwisenote2.modules.repositories;

import android.app.Application;
import android.content.Context;
import com.originb.inkwisenote2.common.ListUtils;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.smartnotes.data.*;
import com.originb.inkwisenote2.common.Strings;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class SmartNotebookRepository {

    private final AtomicNotesDomain atomicNotesDomain;
    private final AtomicNoteEntitiesDao atomicNoteEntitiesDao;
    private final SmartBooksDao smartBooksDao;
    private final SmartBookPagesDao smartBookPagesDao;

    public SmartNotebookRepository() {
        this.atomicNoteEntitiesDao = Repositories.getInstance().getNotesDb().atomicNoteEntitiesDao();
        this.smartBooksDao = Repositories.getInstance().getNotesDb().smartBooksDao();
        this.smartBookPagesDao = Repositories.getInstance().getNotesDb().smartBookPagesDao();
        this.atomicNotesDomain = Repositories.getInstance().getAtomicNotesDomain();
    }

    // Create the data and return the notebook entity
    public Optional<SmartNotebook> initializeNewSmartNotebook(String title,
                                                              String directoryPath,
                                                              NoteType noteType) {
        // Create a directory with the notebook title
        String notebookDirectory = Paths.get(directoryPath, title).toString();
        File notebookDir = new File(notebookDirectory);
        if (!notebookDir.exists()) {
            notebookDir.mkdirs();
        }

        AtomicNoteEntity atomicNoteEntity = atomicNotesDomain.saveAtomicNote(AtomicNotesDomain.constructAtomicNote(
                "",
                notebookDirectory,
                noteType));
        SmartBookEntity smartBookEntity = newSmartBook(title, atomicNoteEntity.getCreatedTimeMillis());
        SmartBookPage smartBookPage = newSmartBookPage(smartBookEntity, atomicNoteEntity, 0);
        SmartNotebook smartNotebook = new SmartNotebook(smartBookEntity, smartBookPage, atomicNoteEntity);
        return Optional.ofNullable(smartNotebook);
    }

    public void deleteSmartNotebook(SmartNotebook smartNotebook) {
        // Delete notebook folder from filesystem if it exists
        try {
            if (!smartNotebook.getAtomicNotes().isEmpty()) {
                AtomicNoteEntity firstNote = smartNotebook.getAtomicNotes().get(0);
                if (firstNote != null && firstNote.getFilepath() != null) {
                    File notebookDir = new File(firstNote.getFilepath());
                    if (notebookDir.exists() && notebookDir.isDirectory()) {
                        // Delete all files in the directory
                        File[] files = notebookDir.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                file.delete();
                            }
                        }
                        // Delete the directory itself
                        notebookDir.delete();
                    }
                }
            }
        } catch (Exception e) {
            // Log error but continue with database deletion
            System.err.println("Error deleting notebook directory: " + e.getMessage());
        }

        // Delete database records
        smartBookPagesDao.deleteSmartBookPages(smartNotebook.getSmartBook().getBookId());

        smartNotebook.getAtomicNotes()
                .stream().map(AtomicNoteEntity::getNoteId)
                .forEach(atomicNoteEntitiesDao::deleteAtomicNote);

        smartBooksDao.deleteSmartBook(smartNotebook.getSmartBook().getBookId());

        EventBus.getDefault().post(new Events.NotebookDeleted(smartNotebook));
    }

    public void deleteNoteFromBook(SmartNotebook smartNotebook, AtomicNoteEntity atomicNote) {
        // will pages allow this to be deleted first?
        if (smartNotebook.getAtomicNotes().size() <= 1) deleteSmartNotebook(smartNotebook);
        else {

            smartBookPagesDao.deleteNotePages(atomicNote.getNoteId());
            atomicNoteEntitiesDao.deleteAtomicNote(atomicNote.getNoteId());

            EventBus.getDefault().post(new Events.NoteDeleted(smartNotebook, atomicNote));
        }
    }

    public void updateNotebook(SmartNotebook smartNotebook, Context context) {
        long updateTime = System.currentTimeMillis();

        SmartBookEntity smartBookEntity = smartNotebook.getSmartBook();

        // means this is a virtual notebook
        if (smartBookEntity.getBookId() == -1) {
            return;
        }

        smartBookEntity.setLastModifiedTimeMillis(System.currentTimeMillis());
        smartBooksDao.updateSmartBook(smartBookEntity);

        for (AtomicNoteEntity atomicNote : smartNotebook.getAtomicNotes()) {
            atomicNote.setLastModifiedTimeMillis(updateTime);
        }
        atomicNotesDomain.updateAtomicNotes(smartNotebook.getAtomicNotes());

        int updateResult = smartBookPagesDao.updateSmartBookPage(smartNotebook.getSmartBookPages());
        EventBus.getDefault().post(new Events.SmartNotebookSaved(smartNotebook, context));
    }

    public SmartNotebook saveSmartNotebook(SmartNotebook smartNotebook, Application context) {
        long updateTime = System.currentTimeMillis();
        SmartBookEntity smartBookEntity = smartNotebook.getSmartBook();
        // means this is not a new notebook
        if (smartBookEntity.getBookId() > -1) {
            return smartNotebook;
        }

        smartBookEntity.setCreatedTimeMillis(updateTime);
        smartBookEntity.setLastModifiedTimeMillis(updateTime);

        long bookId = smartBooksDao.insertSmartBook(smartBookEntity);
        smartBookEntity.setBookId(bookId);

        for (SmartBookPage smartBookPage : smartNotebook.getSmartBookPages()) {
            smartBookPage.setBookId(bookId);
            long id = smartBookPagesDao.insertSmartBookPage(smartBookPage);
            smartBookPage.setId(id);
        }
        // TODO: Shouldn't I save atomic notes as well?
        EventBus.getDefault().post(new Events.SmartNotebookSaved(smartNotebook, context));

        return smartNotebook;
    }

    public List<SmartNotebook> getSmartNotebookContainingNote(long noteId) {
        List<SmartBookPage> pagesOfNote = smartBookPagesDao.getSmartBookPagesOfNote(noteId);
        if (pagesOfNote == null || pagesOfNote.isEmpty()) return new ArrayList<>();

        List<SmartNotebook> smartNotebooks = pagesOfNote.stream().map(SmartBookPage::getBookId)
                .map(this::getSmartNotebooks)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return smartNotebooks;
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

            List<AtomicNoteEntity> atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIds);

            smartNotebooks.add(new SmartNotebook(smartBook, smartBookPages, atomicNoteEntities));
        }

        return smartNotebooks;
    }

    public Set<SmartNotebook> getSmartNotebooksForNoteIds(Set<Long> noteIds) {
        List<SmartBookPage> smartBookPages = smartBookPagesDao.getSmartBookPagesOfNote(noteIds);
        Map<Long, List<SmartBookPage>> bookIdToPageMap = ListUtils.groupBy(smartBookPages, SmartBookPage::getBookId);

        List<SmartBookEntity> smartBooks = smartBooksDao.getSmartBooks(bookIdToPageMap.keySet());
        if (smartBooks == null || smartBooks.isEmpty()) return new HashSet<>();

        List<AtomicNoteEntity> atomicNotes = atomicNotesDomain.getAtomicNotes(noteIds);
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

    public Optional<SmartNotebook> getVirtualSmartNotebooks(String bookTitle, Set<Long> noteIds) {
        List<AtomicNoteEntity> atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIds);
        List<SmartBookPage> smartBookPages = new ArrayList<>();

        int pageIndex = 0;
        for (AtomicNoteEntity note : atomicNoteEntities) {
            smartBookPages.add(new SmartBookPage(-1,
                    note.getNoteId(),
                    pageIndex));
            pageIndex++;
        }
        SmartBookEntity smartBook = new SmartBookEntity(-1, bookTitle,
                System.currentTimeMillis(),
                System.currentTimeMillis());

        return Optional.of(new SmartNotebook(smartBook, smartBookPages, atomicNoteEntities));

    }

    public Optional<SmartNotebook> getSmartNotebooks(long bookId) {
        SmartBookEntity smartBook = smartBooksDao.getSmartbook(bookId);
        if (smartBook == null) return Optional.empty();

        List<SmartBookPage> smartBookPages = smartBookPagesDao.getSmartBookPages(bookId);
        if (smartBookPages == null || smartBookPages.isEmpty()) return Optional.empty();

        Set<Long> noteIds = smartBookPages.stream()
                .map(SmartBookPage::getNoteId)
                .collect(Collectors.toSet());

        List<AtomicNoteEntity> atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIds);

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
        List<AtomicNoteEntity> atomicNotes = atomicNotesDomain.getAtomicNotes(noteIds);
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

    private SmartBookEntity newSmartBook(String title, long createdDateTimeMs) {
        SmartBookEntity smartBookEntity = new SmartBookEntity();
        if (!Strings.isNullOrWhitespace(title)) {
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


    public boolean bookExists(SmartNotebook notebook) {
        SmartBookEntity smartBook = notebook.getSmartBook();
        if (smartBook == null) return false;
        long bookId = smartBook.getBookId();
        SmartBookEntity bookInDb = smartBooksDao.getSmartbook(bookId);
        return bookInDb != null;
    }

    public List<SmartBookPage> getAllSmartBookPages() {
        return smartBookPagesDao.getAllSmartBookPages();
    }
}



























