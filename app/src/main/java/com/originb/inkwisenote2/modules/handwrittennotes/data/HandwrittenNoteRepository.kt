package com.originb.inkwisenote2.modules.handwrittennotes.data

import android.graphics.*
import com.google.android.gms.common.util.Strings
import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectOutputStream
import java.util.*

class HandwrittenNoteRepository {
    private val handwrittenNotesDao: HandwrittenNotesDao =
        Repositories.Companion.getInstance().getNotesDb().handwrittenNotesDao()

    fun saveHandwrittenNoteImage(atomicNote: AtomicNoteEntity?, bitmap: Bitmap?) {
        if (Strings.isEmptyOrWhitespace(atomicNote.getFilepath()) || Objects.isNull(bitmap)) {
            return
        }

        val fullPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".png"
        val thumbnailPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + "-t.png"

        val thumbnail = BitmapFileIoUtils.resizeBitmap(bitmap, BitmapScale.THUMBNAIL.value)

        BitmapFileIoUtils.writeDataToDisk(fullPath, bitmap)
        BitmapFileIoUtils.writeDataToDisk(thumbnailPath, thumbnail)
    }

    fun saveHandwrittenNotePageTemplate(atomicNote: AtomicNoteEntity?, pageTemplate: PageTemplate?) {
        val path = atomicNote.getFilepath()
        if (Objects.isNull(pageTemplate) || Strings.isEmptyOrWhitespace(path)) {
            return
        }

        val fullPath = path + "/" + atomicNote.getFilename() + ".pt"
        BytesFileIoUtils.writeDataToDisk(fullPath, pageTemplate)
    }

    fun saveHandwrittenNotes(
        bookId: Long,
        atomicNote: AtomicNoteEntity?,
        bitmap: Bitmap?,
        pageTemplate: PageTemplate?
    ): Boolean {
        val bitmapHash = getBitmapHash(bitmap)
        val pageTemplateHash = getPageTemplateHash(pageTemplate)

        var noteUpdated = false

        var handwrittenNoteEntity = handwrittenNotesDao
            .getHandwrittenNoteForNote(atomicNote.getNoteId())
        if (handwrittenNoteEntity == null) {
            handwrittenNoteEntity = HandwrittenNoteEntity()
            handwrittenNoteEntity.setNoteId(atomicNote.getNoteId())
            handwrittenNoteEntity.setBookId(bookId)

            val bitmapFilePath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".png"
            handwrittenNoteEntity.setBitmapFilePath(bitmapFilePath)
            handwrittenNoteEntity.setBitmapHash(bitmapHash)

            handwrittenNoteEntity.setCreatedTimeMillis(System.currentTimeMillis())
            handwrittenNoteEntity.setLastModifiedTimeMillis(System.currentTimeMillis())
            saveHandwrittenNoteImage(atomicNote, bitmap)
            handwrittenNotesDao.insertHandwrittenNote(handwrittenNoteEntity)
            noteUpdated = true
        } else if (bitmapHash != null && bitmapHash != handwrittenNoteEntity.bitmapHash) {
            handwrittenNoteEntity.bitmapHash = bitmapHash
            handwrittenNoteEntity.lastModifiedTimeMillis = System.currentTimeMillis()
            saveHandwrittenNoteImage(atomicNote, bitmap)
            handwrittenNotesDao.updateHandwrittenNote(handwrittenNoteEntity)
            noteUpdated = true
        }

        if (handwrittenNoteEntity.pageTemplateHash == null
            && pageTemplateHash != null
        ) {
            handwrittenNoteEntity.pageTemplateFilePath =
                atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".pt"
            handwrittenNoteEntity.pageTemplateHash = pageTemplateHash
            saveHandwrittenNotePageTemplate(atomicNote, pageTemplate)
            handwrittenNotesDao.updateHandwrittenNote(handwrittenNoteEntity)
        } else if (pageTemplateHash != null && pageTemplateHash != handwrittenNoteEntity.pageTemplateHash) {
            handwrittenNoteEntity.pageTemplateHash = bitmapHash
            handwrittenNoteEntity.lastModifiedTimeMillis = System.currentTimeMillis()
            saveHandwrittenNotePageTemplate(atomicNote, pageTemplate)
            handwrittenNotesDao.updateHandwrittenNote(handwrittenNoteEntity)
        }

        return noteUpdated
    }

    fun getNoteImage(atomicNote: AtomicNoteEntity, imageScale: BitmapScale): HandwrittenNoteWithImage {
        val handwrittenNoteEntity = handwrittenNotesDao.getHandwrittenNoteForNote(atomicNote.noteId)
        val handwrittenNoteWithImage = HandwrittenNoteWithImage()

        handwrittenNoteWithImage.handwrittenNoteEntity = handwrittenNoteEntity
        val fullPath = if (BitmapScale.FULL_SIZE == imageScale) {
            atomicNote.filepath + "/" + atomicNote.filename + ".png"
        } else {
            atomicNote.filepath + "/" + atomicNote.filename + "-t.png"
        }
        handwrittenNoteWithImage.noteImage = BitmapFileIoUtils.readBitmapFromFile(fullPath, BitmapScale.FULL_SIZE.value)
        return handwrittenNoteWithImage
    }

    fun getPageTemplate(atomicNote: AtomicNoteEntity): Optional<PageTemplate?>? {
        val fullPath = atomicNote.filepath + "/" + atomicNote.filename + ".pt"
        return BytesFileIoUtils.readDataFromDisk<PageTemplate>(fullPath, PageTemplate::class.java)!!
    }

    fun deleteHandwrittenNote(atomicNote: AtomicNoteEntity?) {
        val bitmapPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".png"
        BitmapFileIoUtils.deleteBitmap(bitmapPath)
        val thumbnailPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + "-t.png"
        BitmapFileIoUtils.deleteBitmap(thumbnailPath)

        val fullPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".pt"
        val noteFile = File(fullPath)
        noteFile.delete()
    }

    private fun getBitmapHash(bitmap: Bitmap?): String? {
        val bitmapStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream)
        return HashUtils.calculateSha256(bitmapStream.toByteArray())
    }

    private fun getPageTemplateHash(pageTemplate: PageTemplate?): String? {
        try {
            val byteStream = ByteArrayOutputStream()
            ObjectOutputStream(byteStream).use { objectStream ->
                objectStream.writeObject(pageTemplate) // Serialize the object
            }
            return HashUtils.calculateSha256(byteStream.toByteArray())
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }
}
