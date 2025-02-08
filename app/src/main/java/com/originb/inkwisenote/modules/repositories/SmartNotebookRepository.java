package com.originb.inkwisenote.modules.repositories;

import android.graphics.Bitmap;
import com.originb.inkwisenote.data.dao.AtomicNoteEntitiesDao;
import com.originb.inkwisenote.data.dao.SmartBookPagesDao;
import com.originb.inkwisenote.data.dao.SmartBooksDao;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookPage;
import com.originb.inkwisenote.data.notedata.PageTemplate;
import com.originb.inkwisenote.io.NoteBitmapFiles;
import com.originb.inkwisenote.io.PageTemplateFiles;
import com.originb.inkwisenote.modules.commonutils.Strings;

import java.security.cert.PKIXRevocationChecker;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SmartNotebookRepository {

    //    private NoteMetaFiles noteMetaFiles;
    private final NoteBitmapFiles noteBitmapFiles;
    private final PageTemplateFiles pageTemplateFiles;
//    private NoteOcrTextDao noteOcrTextDao;
//    private NoteTermFrequencyDao noteTermFrequencyDao;
//
//    private NoteRelationDao noteRelationDao;

    private final AtomicNoteEntitiesDao atomicNoteEntitiesDao;
    private final SmartBooksDao smartBooksDao;
    private final SmartBookPagesDao smartBookPagesDao;

    public SmartNotebookRepository() {
//        this.noteMetaFiles = Repositories.getInstance().getNoteMetaRepository();
        this.noteBitmapFiles = Repositories.getInstance().getBitmapRepository();
        this.pageTemplateFiles = Repositories.getInstance().getPageTemplateFiles();
//        this.noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();
//        this.noteTermFrequencyDao = Repositories.getInstance().getNotesDb().noteTermFrequencyDao();
//        this.noteRelationDao = Repositories.getInstance().getNotesDb().noteRelationDao();

        this.atomicNoteEntitiesDao = Repositories.getInstance().getNotesDb().atomicNoteEntitiesDao();
        this.smartBooksDao = Repositories.getInstance().getNotesDb().smartBooksDao();
        this.smartBookPagesDao = Repositories.getInstance().getNotesDb().smartBookPagesDao();
    }


    // Create the data and return the notebook entity
    public Optional<SmartNotebook> initializeNewSmartNote(String title,
                                                          String directoryPath,
                                                          Bitmap bitmap,
                                                          PageTemplate pageTemplate) {

        AtomicNoteEntity atomicNoteEntity = newHandwrittenNote("", directoryPath);

        noteBitmapFiles.saveBitmap(atomicNoteEntity.getNoteId(),
                atomicNoteEntity.getFilepath(),
                atomicNoteEntity.getFilename(),
                bitmap);

        pageTemplateFiles.savePageTemplate(atomicNoteEntity.getNoteId(),
                atomicNoteEntity.getFilepath(),
                atomicNoteEntity.getFilename(),
                pageTemplate);

        SmartBookEntity smartBookEntity = newSmartBook(title, atomicNoteEntity.getCreatedTimeMillis());

        SmartBookPage smartBookPage = newSmartBookPage(smartBookEntity, atomicNoteEntity);

        SmartNotebook smartNotebook = new SmartNotebook(smartBookEntity, smartBookPage, atomicNoteEntity);
        return Optional.ofNullable(smartNotebook);
    }

    public Optional<Bitmap> getNoteImage(AtomicNoteEntity atomicNote, boolean loadFullImage) {
        if (loadFullImage) {
            Optional<Bitmap> bitmapOpt = noteBitmapFiles.getFullBitmap(atomicNote.getNoteId());
            return bitmapOpt;
        }

        return noteBitmapFiles.getThumbnail(atomicNote.getNoteId());
    }

    public Optional<SmartNotebook> getSmartNotebookContainingNote(long noteId) {
        List<SmartBookPage> pagesOfNote = smartBookPagesDao.getSmartBookPagesOfNote(noteId);
        if (pagesOfNote == null || pagesOfNote.isEmpty()) return Optional.empty();

        // TODO: only get the first page for now. We will fetch more later
        long bookId = pagesOfNote.stream().findFirst().map(SmartBookPage::getBookId).get();

        return getSmartNotebook(bookId);
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

    private AtomicNoteEntity newHandwrittenNote(String filename, String filepath) {
        long createdTimeMillis = System.currentTimeMillis();
        AtomicNoteEntity atomicNoteEntity = new AtomicNoteEntity();
        atomicNoteEntity.setCreatedTimeMillis(createdTimeMillis);

        if (Strings.isNullOrWhitespace(filename)) {
            atomicNoteEntity.setFilename(createdTimeMillis + ".png");
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
            smartBookEntity.setTitle(msToDateTime(createdDateTimeMs));
        } else {
            smartBookEntity.setTitle(title);
        }
        long bookId = smartBooksDao.insertSmartBook(smartBookEntity);
        smartBookEntity.setBookId(bookId);

        return smartBookEntity;
    }

    private SmartBookPage newSmartBookPage(SmartBookEntity smartBookEntity, AtomicNoteEntity atomicNoteEntity) {

        SmartBookPage smartBookPage = new SmartBookPage(smartBookEntity.getBookId(),
                atomicNoteEntity.getNoteId());
        long id = smartBookPagesDao.insertSmartBook(smartBookPage);
        smartBookPage.setId(id);

        return smartBookPage;
    }

    private static String msToDateTime(long createdTimeMillis) {
        Instant instant = Instant.ofEpochMilli(createdTimeMillis);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        return formatter.format(instant);
    }
}



























