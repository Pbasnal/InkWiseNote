package com.originb.inkwisenote2.modules.ocr.worker

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.work.*
import com.originb.inkwisenote2.common.BitmapScale
import com.originb.inkwisenote2.common.ListUtils
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.config.AppSecrets
import com.originb.inkwisenote2.config.ConfigReader
import com.originb.inkwisenote2.functionalUtils.Try
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.ocr.data.AzureOcrResult
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao
import com.originb.inkwisenote2.modules.ocr.data.OcrService.AnalyzeImageTask
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer
import java.util.function.Predicate

class TextParsingWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val handwrittenNoteRepository: HandwrittenNoteRepository =
        Repositories.Companion.getInstance().getHandwrittenNoteRepository()

    private val smartNotebookRepository: SmartNotebookRepository =
        Repositories.Companion.getInstance().getSmartNotebookRepository()

    private val appSecrets: AppSecrets = ConfigReader.Companion.getInstance().getAppConfig().getAppSecrets()
    private val noteOcrTextDao: NoteOcrTextDao =
        Repositories.Companion.getInstance().getNotesDb().noteOcrTextDao()

    private val logger = Logger("TextParsingWorker")

    override fun doWork(): Result {
        Try.Companion.to<Long>(Callable<Long> { inputData.getLong("book_id", -1) }, logger).get()
            .filter(Predicate<Long> { noteId: Long -> this.isNoteIdGreaterThan0(noteId) })
            .ifPresent(Consumer<Long> { bookId: Long -> this.parseTextForNotebook(bookId) })

        return Result.success()
    }

    fun parseTextForNotebook(bookId: Long) {
        logger.debug("Parsing text from a notebook (new flow): $bookId")

        val smartBookOpt = smartNotebookRepository.getSmartNotebooks(bookId)
        if (!smartBookOpt!!.isPresent) return
        val smartNotebook = smartBookOpt.get()

        logger.debug("Notebook bookId: " + bookId + " contains number of notes: " + smartNotebook.getAtomicNotes().size)
        for (atomicNote in smartNotebook.getAtomicNotes()) {
            if (!parseTextForHandwrittenNote(atomicNote)) {
                onFailure(bookId)
            }
        }

        logger.debug("Scheduling text processing work for book: $bookId")

        WorkManagerBus.scheduleWorkForTextProcessingForBook(applicationContext, bookId)
    }

    fun parseTextForHandwrittenNote(atomicNote: AtomicNoteEntity): Boolean {
        val handwrittenNoteWithImage = handwrittenNoteRepository.getNoteImage(atomicNote, BitmapScale.FULL_SIZE)
        val handwrittenNoteEntity = handwrittenNoteWithImage!!.handwrittenNoteEntity
        val bitmapOpt = handwrittenNoteWithImage.noteImage
        if (!bitmapOpt!!.isPresent) {
            logger.error("Handwritten note doesn't have image", handwrittenNoteEntity)
            return false
        }
        val noteBitmap = bitmapOpt.get()
        val noteOcrTextDb = noteOcrTextDao.readTextFromDb(atomicNote.noteId)
        if (noteOcrTextDb != null && noteOcrTextDb.noteHash == handwrittenNoteEntity.bitmapHash) {
            logger.debug("Note ocr has the latest text, skipping", handwrittenNoteEntity)
            return true
        }

        val azureResultOpt = applyAzureOcr(noteBitmap)
        if (!azureResultOpt.isPresent) {
            logger.error("Failed to get text using OCR", handwrittenNoteEntity)
            return false
        }
        val noteOcrText = NoteOcrText(
            atomicNote.noteId,
            handwrittenNoteEntity.bitmapHash,
            azureResultOpt.get().readResult!!.content
        )

        if (noteOcrTextDb == null) {
            logger.debug("Inserting extracted text", ListUtils.listOf(noteOcrText, handwrittenNoteEntity))
            noteOcrTextDao.insertTextToDb(noteOcrText)
        } else {
            logger.debug("Updating extracted text", ListUtils.listOf(noteOcrText, handwrittenNoteEntity))
            noteOcrTextDao.updateTextToDb(noteOcrText)
        }
        return true
    }

    private fun onFailure(bookId: Long): Result {
        WorkManagerBus.scheduleWorkForFindingRelatedNotesForBook(applicationContext, bookId)
        return Result.failure()
    }

    private fun isNoteIdGreaterThan0(noteId: Long): Boolean {
        if (noteId == -1L) {
            logger.error("Got incorrect note id (-1) as input: $noteId")
            return false
        }
        return true
    }

    private fun applyAzureOcr(bitmap: Bitmap): Optional<AzureOcrResult?> {
        Log.d("got bitmap", "bitmap")
        val ocrResult: Optional<AzureOcrResult?> = Try.Companion.to<AzureOcrResult?>(
            Callable<AzureOcrResult?> {
                val byteArrayOutputStream = ByteArrayOutputStream()
                // Compress the bitmap into the ByteArrayOutputStream
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

                // Convert the ByteArrayOutputStream to an InputStream
                val byteArray = byteArrayOutputStream.toByteArray()
                val imageStream: InputStream = ByteArrayInputStream(byteArray)
                val imageTask = AnalyzeImageTask(appSecrets)
                val azureOcrResult = imageTask.runOcr(imageStream)

                Log.d("NoteActivity", "Ocr result: $azureOcrResult")
                azureOcrResult
            }, Logger("TextParsingJob")
        )
            .logIfError("Failed to convert handwriting to text")
            .get()
        return ocrResult
    }
}
