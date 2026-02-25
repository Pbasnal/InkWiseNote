package com.originb.inkwisenote2.modules.handwrittennotes.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.common.util.Strings
import com.originb.inkwisenote2.common.BitmapFileIoUtils
import com.originb.inkwisenote2.common.BitmapFileIoUtils.deleteBitmap
import com.originb.inkwisenote2.common.BitmapFileIoUtils.readBitmapFromFile
import com.originb.inkwisenote2.common.BitmapScale
import com.originb.inkwisenote2.common.BytesFileIoUtils.readDataFromDisk
import com.originb.inkwisenote2.common.BytesFileIoUtils.writeDataToDisk
import com.originb.inkwisenote2.common.HashUtils.calculateSha256
import com.originb.inkwisenote2.common.HashUtils.getBitmapHash
import com.originb.inkwisenote2.common.HashUtils.getPageTemplateHash
import com.originb.inkwisenote2.modules.handwrittennotes.ui.ThumbnailGenerator
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.modules.backgroundjobs.Events.HandwrittenNoteSaved
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class HandwrittenNoteRepository(
    private val handwrittenNotesDao: HandwrittenNotesDao,
    private val atomicNotesDomain: AtomicNotesDomain
) {
    private val logger = Logger("HandwrittenNoteRepository")

    // Maps noteId to a lock object for synchronizing file operations per note
    private val noteLocks: MutableMap<Long?, Any> = ConcurrentHashMap<Long?, Any>()

    private fun getLockForNote(noteId: Long): Any {
        return noteLocks.computeIfAbsent(noteId) { k: Long? -> Any() }
    }

    fun saveHandwrittenNoteImage(note: AtomicNoteEntity, bitmap: Bitmap?, strokes: MutableList<Stroke>?) {
        if (bitmap == null || Strings.isEmptyOrWhitespace(note.filepath)) return

        try {
            val thumbnail = ThumbnailGenerator.generateThumbnail(bitmap, strokes)
            BitmapFileIoUtils.writeDataToDisk(NoteFileStorage.getImagePath(note), bitmap)
            if (thumbnail != null) {
                BitmapFileIoUtils.writeDataToDisk(NoteFileStorage.getThumbnailPath(note), thumbnail)
            }
        } catch (ex: Exception) {
            logger.exception("Error saving bitmap for note: " + note.noteId, ex)
        }
    }

    fun saveHandwrittenNotePageTemplate(atomicNote: AtomicNoteEntity, pageTemplate: PageTemplate?) {
        val path = atomicNote.filepath ?: ""
        if (Objects.isNull(pageTemplate) || Strings.isEmptyOrWhitespace(path)) {
            return
        }

        val fullPath = path + "/" + (atomicNote.filename ?: "") + ".pt"
        writeDataToDisk<PageTemplate?>(fullPath, pageTemplate)
    }

    fun saveHandwrittenNotes(
        bookId: Long, atomicNote: AtomicNoteEntity, bitmap: Bitmap,
        pageTemplate: PageTemplate?, strokes: MutableList<Stroke>?, context: Context?
    ): Boolean {
        val note = atomicNote.copy()
        // Use synchronized on the note ID string intern to be thread-safe across instances
        synchronized(getLockForNote(note.noteId)) {
            val bitmapHash = getBitmapHash(bitmap) // Move hashing to a Utility
            var entity = handwrittenNotesDao.getHandwrittenNoteForNote(note.noteId)

            val isNew = (entity == null)
            if (isNew) {
                entity = createNewEntity(bookId, note, bitmapHash)
            }

            var updated = false
            if (isNew || bitmapHash != entity!!.bitmapHash) {
                updateVisualData(entity!!, note, bitmap, strokes, bitmapHash)
                updated = true
            }

            // Handle Template Hash logic...
            processTemplateUpdate(entity, note, pageTemplate)

            if (updated) {
                handwrittenNotesDao.upsert(entity) // Use a Room @Upsert
                if (note.filepath != atomicNote.filepath) {
                    // Update the filepath in the database
                    atomicNotesDomain.updateAtomicNote(note)
                }
                EventBus.getDefault().post(HandwrittenNoteSaved(bookId, note, context))
            }
            return updated
        }
    }

    private fun createNewEntity(bookId: Long, note: AtomicNoteEntity, hash: String?): HandwrittenNoteEntity {
        val entity = HandwrittenNoteEntity()
        entity.noteId = note.noteId
        entity.bookId = bookId
        entity.bitmapFilePath = NoteFileStorage.getImagePath(note)
        entity.bitmapHash = hash
        entity.createdTimeMillis = System.currentTimeMillis()
        entity.lastModifiedTimeMillis = System.currentTimeMillis()
        return entity
    }

    private fun updateVisualData(
        entity: HandwrittenNoteEntity, note: AtomicNoteEntity,
        bitmap: Bitmap?, strokes: MutableList<Stroke>?, hash: String?
    ) {
        entity.bitmapHash = hash
        entity.lastModifiedTimeMillis = System.currentTimeMillis()
        saveHandwrittenNoteImage(note, bitmap, strokes)
        saveHandwrittenNoteMarkdown(note, strokes)
    }

    private fun processTemplateUpdate(
        entity: HandwrittenNoteEntity,
        note: AtomicNoteEntity,
        pageTemplate: PageTemplate?
    ) {
        val hash = getPageTemplateHash(pageTemplate)
        if (hash == null) {
            Log.e("HandwrittenNoteRepository", "Failed to generate page template hash")
            return
        }

        val pageTemplateIsSame = hash == entity.pageTemplateHash
        if (pageTemplateIsSame) return

        val isNew = entity.pageTemplateHash == null

        if (isNew) {
            entity.pageTemplateFilePath = NoteFileStorage.getTemplatePath(note)
        }

        entity.pageTemplateHash = hash
        entity.lastModifiedTimeMillis = System.currentTimeMillis()
        saveHandwrittenNotePageTemplate(note, pageTemplate)
        handwrittenNotesDao.updateHandwrittenNote(entity)
    }

    fun getNoteImage(atomicNote: AtomicNoteEntity, imageScale: BitmapScale?): HandwrittenNoteWithImage {
        val handwrittenNoteEntity = handwrittenNotesDao.getHandwrittenNoteForNote(atomicNote.noteId)
        val handwrittenNoteWithImage = HandwrittenNoteWithImage()

        handwrittenNoteWithImage.handwrittenNoteEntity = handwrittenNoteEntity

        val basePath = (atomicNote.filepath ?: "") + "/" + (atomicNote.filename ?: "")
        val fullPath = if (BitmapScale.FULL_SIZE == imageScale) "$basePath.png" else "$basePath-t.png"
        handwrittenNoteWithImage.noteImage = readBitmapFromFile(fullPath, BitmapScale.FULL_SIZE).orElse(null)
        return handwrittenNoteWithImage
    }

    fun getPageTemplate(atomicNote: AtomicNoteEntity): Optional<PageTemplate> {
        val fullPath = (atomicNote.filepath ?: "") + "/" + (atomicNote.filename ?: "") + ".pt"
        val opt = readDataFromDisk<PageTemplate>(fullPath, PageTemplate::class.java)
        return Optional.ofNullable(opt.orElse(null))
    }

    fun deleteHandwrittenNote(atomicNote: AtomicNoteEntity) {
        val base = (atomicNote.filepath ?: "") + "/" + (atomicNote.filename ?: "")
        val bitmapPath = base + ".png"
        deleteBitmap(bitmapPath)
        val thumbnailPath = base + "-t.png"
        deleteBitmap(thumbnailPath)

        val templPath = base + ".pt"
        val noteFile = File(templPath)
        noteFile.delete()

        // Delete markdown file
        val markdownPath = base + ".md"
        val markdownFile = File(markdownPath)
        markdownFile.delete()

        val notebookDir = File(atomicNote.filepath ?: "")
        if (notebookDir.exists() && notebookDir.isDirectory()) {
            // Delete all files in the directory
            val files = notebookDir.listFiles()
            if (files == null || files.size == 0) {
                notebookDir.delete()
            }
        }
    }

    fun saveHandwrittenNoteMarkdown(atomicNote: AtomicNoteEntity, strokes: MutableList<Stroke>?): Boolean {
        if (Strings.isEmptyOrWhitespace(atomicNote.filepath) || strokes == null) {
            return false
        }

        val markdownPath = (atomicNote.filepath ?: "") + "/" + (atomicNote.filename ?: "") + ".md"
        try {
            FileWriter(markdownPath).use { writer ->
                val markdown = StringBuilder()
                // Add markdown header
                markdown.append("# Handwritten Note\n\n")

                // Begin inkwise code block
                markdown.append("```inkwise\n")

                // Convert strokes to JSON-like format within the code block
                for (stroke in strokes) {
                    markdown.append(NoteFileStorage.serializeStroke(stroke)).append("\n")
                }

                // End inkwise code block
                markdown.append("```\n")

                writer.write(markdown.toString())
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }


    private fun getStrokesHash(strokes: MutableList<Stroke?>?): String? {
        if (strokes == null || strokes.isEmpty()) {
            return null
        }

        try {
            val byteStream = ByteArrayOutputStream()
            ObjectOutputStream(byteStream).use { objectStream ->
                objectStream.writeObject(strokes)
            }
            return calculateSha256(byteStream.toByteArray())
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }

    fun readHandwrittenNoteMarkdown(atomicNote: AtomicNoteEntity): MutableList<Stroke> {
        if (Strings.isEmptyOrWhitespace(atomicNote.filepath)) {
            return ArrayList<Stroke>()
        }

        val markdownPath = (atomicNote.filepath ?: "") + "/" + (atomicNote.filename ?: "") + ".md"
        val file = File(markdownPath)
        if (!file.exists() || !file.isFile()) {
            return ArrayList<Stroke>()
        }

        return readStrokesFromMarkdown(markdownPath)
    }

    private fun readStrokesFromMarkdown(filePath: String): MutableList<Stroke> {
        val strokes: MutableList<Stroke> = ArrayList<Stroke>()
        var inCodeBlock = false

        try {
            BufferedReader(FileReader(filePath)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    val trimmed = line.trim()
                    // Check for beginning of inkwise code block
                    if (trimmed == "```inkwise") {
                        inCodeBlock = true
                        line = reader.readLine()
                        continue
                    }

                    // Check for end of code block
                    if (trimmed == "```") {
                        inCodeBlock = false
                        line = reader.readLine()
                        continue
                    }

                    // Parse stroke data if within code block
                    if (inCodeBlock && trimmed.isNotEmpty()) {
                        try {
                            val stroke = NoteFileStorage.deserializeStroke(line)
                            strokes.add(stroke)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    line = reader.readLine()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return strokes
    }

    fun getStrokes(noteId: Long): MutableList<Stroke> {
        val note = atomicNotesDomain.getAtomicNote(noteId)
        return readHandwrittenNoteMarkdown(note)
    }

    private fun getParentDirectory(filepath: String): String {
        if (Strings.isEmptyOrWhitespace(filepath)) {
            return ""
        }

        val lastSlashIndex = filepath.lastIndexOf('/')
        if (lastSlashIndex > 0) {
            return filepath.take(lastSlashIndex)
        }
        return filepath
    }
}
