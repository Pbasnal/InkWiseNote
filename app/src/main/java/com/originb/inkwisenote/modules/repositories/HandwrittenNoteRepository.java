package com.originb.inkwisenote.modules.repositories;

import android.graphics.Bitmap;
import com.google.android.gms.common.util.Strings;
import com.originb.inkwisenote.constants.BitmapScale;
import com.originb.inkwisenote.constants.Returns;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookPage;
import com.originb.inkwisenote.data.notedata.PageTemplate;
import com.originb.inkwisenote.io.NoteBitmapFiles;
import com.originb.inkwisenote.io.PageTemplateFiles;
import com.originb.inkwisenote.io.utils.BitmapFileIoUtils;
import com.originb.inkwisenote.io.utils.BytesFileIoUtils;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

public class HandwrittenNoteRepository {

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

    public void saveHandwrittenNotes(AtomicNoteEntity atomicNote, Bitmap bitmap, PageTemplate pageTemplate) {
        saveHandwrittenNoteImage(atomicNote, bitmap);
        saveHandwrittenNotePageTemplate(atomicNote, pageTemplate);
    }

    public Optional<Bitmap> getNoteImage(AtomicNoteEntity atomicNote, boolean loadFullImage) {
        String fullPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".png";
        if (loadFullImage) {
            return BitmapFileIoUtils.readBitmapFromFile(fullPath, BitmapScale.FULL_SIZE.getResult());
        }

        return BitmapFileIoUtils.readBitmapFromFile(fullPath, BitmapScale.THUMBNAIL.getResult());
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
}
