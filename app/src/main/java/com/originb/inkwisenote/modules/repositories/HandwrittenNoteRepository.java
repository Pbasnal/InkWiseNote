package com.originb.inkwisenote.modules.repositories;

import android.graphics.Bitmap;
import com.google.android.gms.common.util.Strings;
import com.originb.inkwisenote.constants.BitmapScale;
import com.originb.inkwisenote.data.dao.handwrittennotes.HandwrittenNotesDao;
import com.originb.inkwisenote.data.entities.handwrittennotedata.HandwrittenNoteEntity;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.notedata.PageTemplate;
import com.originb.inkwisenote.io.utils.BitmapFileIoUtils;
import com.originb.inkwisenote.io.utils.BytesFileIoUtils;
import com.originb.inkwisenote.io.utils.HashUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.util.Objects;
import java.util.Optional;

public class HandwrittenNoteRepository {

    private final HandwrittenNotesDao handwrittenNotesDao;

    public HandwrittenNoteRepository() {
        this.handwrittenNotesDao = Repositories.getInstance().getNotesDb().handwrittenNotesDao();
    }

    public void saveHandwrittenNoteImage(AtomicNoteEntity atomicNote, Bitmap bitmap) {
        if (Strings.isEmptyOrWhitespace(atomicNote.getFilepath()) || Objects.isNull(bitmap)) {
            return;
        }

        String fullPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".png";
        BitmapFileIoUtils.writeDataToDisk(fullPath, bitmap);
    }

    public void saveHandwrittenNotePageTemplate(AtomicNoteEntity atomicNote, PageTemplate pageTemplate) {
        String path = atomicNote.getFilepath();
        if (Objects.isNull(pageTemplate) || Strings.isEmptyOrWhitespace(path)) {
            return;
        }

        String fullPath = path + "/" + atomicNote.getFilename() + ".pt";
        BytesFileIoUtils.writeDataToDisk(fullPath, pageTemplate);
    }

    public void saveHandwrittenNotes(long bookId, AtomicNoteEntity atomicNote, Bitmap bitmap, PageTemplate pageTemplate) {
        String bitmapHash = getBitmapHash(bitmap);
        String pageTemplateHash = getPageTemplateHash(pageTemplate);

        HandwrittenNoteEntity handwrittenNoteEntity = handwrittenNotesDao
                .getHandwrittenNoteForNote(atomicNote.getNoteId());
        if (handwrittenNoteEntity == null) {
            handwrittenNoteEntity = new HandwrittenNoteEntity();
            handwrittenNoteEntity.setNoteId(atomicNote.getNoteId());
            handwrittenNoteEntity.setBookId(bookId);

            String bitmapFilePath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".png";
            handwrittenNoteEntity.setBitmapFilePath(bitmapFilePath);
            handwrittenNoteEntity.setBitmapHash(bitmapHash);

            handwrittenNoteEntity.setCreatedTimeMillis(System.currentTimeMillis());
            handwrittenNoteEntity.setLastModifiedTimeMillis(System.currentTimeMillis());
            saveHandwrittenNoteImage(atomicNote, bitmap);
            handwrittenNotesDao.insertHandwrittenNote(handwrittenNoteEntity);
        } else if (bitmapHash != null && !bitmapHash.equals(handwrittenNoteEntity.getBitmapHash())) {
            handwrittenNoteEntity.setBitmapHash(bitmapHash);
            handwrittenNoteEntity.setLastModifiedTimeMillis(System.currentTimeMillis());
            saveHandwrittenNoteImage(atomicNote, bitmap);
            handwrittenNotesDao.updateHandwrittenNote(handwrittenNoteEntity);
        }

        if (handwrittenNoteEntity.getPageTemplateHash() == null
                && pageTemplateHash != null) {
            handwrittenNoteEntity.setPageTemplateFilePath(atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".pt");
            handwrittenNoteEntity.setPageTemplateHash(pageTemplateHash);
            saveHandwrittenNotePageTemplate(atomicNote, pageTemplate);
            handwrittenNotesDao.updateHandwrittenNote(handwrittenNoteEntity);
        } else if (pageTemplateHash != null && !pageTemplateHash.equals(handwrittenNoteEntity.getPageTemplateHash())) {
            handwrittenNoteEntity.setPageTemplateHash(bitmapHash);
            handwrittenNoteEntity.setLastModifiedTimeMillis(System.currentTimeMillis());
            saveHandwrittenNotePageTemplate(atomicNote, pageTemplate);
            handwrittenNotesDao.updateHandwrittenNote(handwrittenNoteEntity);
        }
    }

    public HandwrittenNoteWithImage getNoteImage(AtomicNoteEntity atomicNote, boolean loadFullImage) {
        HandwrittenNoteEntity handwrittenNoteEntity = handwrittenNotesDao.getHandwrittenNoteForNote(atomicNote.getNoteId());
        HandwrittenNoteWithImage handwrittenNoteWithImage = new HandwrittenNoteWithImage();

        handwrittenNoteWithImage.handwrittenNoteEntity = handwrittenNoteEntity;

        String fullPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".png";
        if (loadFullImage) {
            handwrittenNoteWithImage.noteImage = BitmapFileIoUtils.readBitmapFromFile(fullPath, BitmapScale.FULL_SIZE.getResult());
        }

        handwrittenNoteWithImage.noteImage = BitmapFileIoUtils.readBitmapFromFile(fullPath, BitmapScale.THUMBNAIL.getResult());

        return handwrittenNoteWithImage;
    }

    public Optional<PageTemplate> getPageTemplate(AtomicNoteEntity atomicNote) {
        String fullPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".pt";
        return BytesFileIoUtils.readDataFromDisk(fullPath, PageTemplate.class);
    }

    public void deleteHandwrittenNote(AtomicNoteEntity atomicNote) {
        String bitmapPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".png";
        BitmapFileIoUtils.deleteBitmap(bitmapPath);

        String fullPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".pt";
        File noteFile = new File(fullPath);
        noteFile.delete();
    }

    private String getBitmapHash(Bitmap bitmap) {
        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
        return HashUtils.calculateSha256(bitmapStream.toByteArray());
    }

    private String getPageTemplateHash(PageTemplate pageTemplate) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
                objectStream.writeObject(pageTemplate); // Serialize the object
            }
            return HashUtils.calculateSha256(byteStream.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
