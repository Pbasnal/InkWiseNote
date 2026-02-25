package com.originb.inkwisenote2.modules.noterelation.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.common.util.CollectionUtils
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.common.isNumber
import com.originb.inkwisenote2.functionalUtils.Either
import com.originb.inkwisenote2.functionalUtils.Function2
import com.originb.inkwisenote2.functionalUtils.Try
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteStatus
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus.scheduleWorkForFindingRelatedNotesForBook
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.noterelation.service.NoteTfIdfLogic
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable

class TextProcessingWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val noteTfIdfLogic: NoteTfIdfLogic,
    private val noteOcrTextDao: NoteOcrTextsDao,
    private val textNotesDao: TextNotesDao,
    private val atomicNotesDomain: AtomicNotesDomain
) : Worker(context, workerParams) {
    private val logger = Logger("TextProcessingWorker")

    private data class ProcessingInput(var bookId: Long = 0, var noteId: Long = 0)

    override fun doWork(): Result {
        val result = Try.to(
            Callable {
                val bookId = getInputData().getLong("book_id", -1)
                val noteId = getInputData().getLong("note_id", -1)
                ProcessingInput(bookId, noteId)
            },
            logger
        ).get()
        Optional.ofNullable(result)
            .filter { input -> isNoteIdGreaterThan0(input) }
            .ifPresent { input -> processTextForNotebook(input) }
        return Result.success()
    }

    private fun processTextForNotebook(processingInput: ProcessingInput) {
        val bookId = processingInput.bookId
        val noteId = processingInput.noteId
        logger.debug("Processing text of book (new flow). noteId: $noteId")
        val atomicNote = atomicNotesDomain.getAtomicNote(noteId)


        val text: String?
        if (NoteType.HANDWRITTEN_PNG.toString() == atomicNote.noteType) {
            text = getHandwrittenNoteText(atomicNote)
        } else {
            text = getTextNoteText(atomicNote.noteId)
        }
        processTextForHandwrittenNote(atomicNote, text)

        EventBus.getDefault().post(NoteStatus(bookId, TextProcessingStage.TOKENIZATION))
        scheduleWorkForFindingRelatedNotesForBook(applicationContext, bookId, noteId)
    }

    private fun getTextNoteText(bookId: Long): String? {
        val textNoteEntity = textNotesDao.getTextNoteForNote(bookId)
        return textNoteEntity.noteText
    }

    private fun getHandwrittenNoteText(atomicNote: AtomicNoteEntity): String {
        val noteOcrTexts = noteOcrTextDao.readTextFromDb(atomicNote.noteId)
        logger.debug("Ocr text of note: " + atomicNote.noteId, noteOcrTexts)

        return noteOcrTexts.extractedText
    }

    private fun processTextForHandwrittenNote(atomicNote: AtomicNoteEntity, text: String?) {
        val eitherTerms: Either<Exception?, DocumentTerms?> = handleException<Long?, String?, DocumentTerms?>(
            { noteId: Long?, text: String? -> this.extractTermsFromNote(noteId!!, text) },
            atomicNote.noteId, text
        )
        handleException<Long?, DocumentTerms?, Long?>({ noteId: Long?, documentTerms: DocumentTerms? ->
            this.createBiRelationalGraph(
                noteId!!,
                documentTerms!!
            )
        }, atomicNote.noteId, eitherTerms.result)
    }

    private fun isNoteIdGreaterThan0(processingInput: ProcessingInput): Boolean {
        if (processingInput.bookId == -1L || processingInput.noteId == -1L) {
            logger.error(
                ("Got incorrect note id (-1) as input: bookId: "
                        + processingInput.bookId + ", noteId: " + processingInput.noteId)
            )
            return false
        }
        return true
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A, B, R> handleException(
        function: Function2<A?, B?, R?>,
        input1: A?,
        input2: B?
    ): Either<Exception?, R?> {
        return try {
            Either.result(function.apply(input1, input2)) as Either<Exception?, R?>
        } catch (ex: Exception) {
            Log.e("TextProcessingJob", "failed to process", ex)
            Either.error(ex) as Either<Exception?, R?>
        }
    }

    private fun extractTermsFromNote(noteId: Long, text: String?): DocumentTerms {
        var text = text
        val documentTerms = DocumentTerms()
        documentTerms.documentId = noteId

        if (text == null || text.isEmpty()) {
            logger.debug("Note has empty text, skipping: " + noteId, text)
            return documentTerms // Return an empty list for null or empty text
        }

        // Step 1: Normalize the text to lowercase
        text = text.lowercase()

        // Step 2: Remove non-alphanumeric characters except spaces
        text = text.replace("[^a-z0-9\\s]".toRegex(), "")

        // Step 3: Split the text into terms by whitespace
        val terms = text.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        documentTerms.terms = ArrayList<String>()
        for (term in terms) {
            if (term.isEmpty()) continue
            if (isNumber(term)) continue

            documentTerms.terms.add(term)
        }
        logger.debug("Extracted document terms", documentTerms)
        return documentTerms
    }

    private fun createBiRelationalGraph(noteId: Long, documentTerms: DocumentTerms): Long? {
        if (Objects.isNull(documentTerms)) {
            logger.debug("empty document terms, skipping: $noteId")
            return null
        }
        if (CollectionUtils.isEmpty(documentTerms.terms)) {
            logger.debug("terms list is empty, skipping: $noteId")
            return documentTerms.documentId
        }
        val docId = documentTerms.documentId
        val terms = documentTerms.terms
        noteTfIdfLogic.addOrUpdateNote(docId, terms)
        return docId
    }

    private class DocumentTerms {
        var documentId: Long = 0
        var terms: MutableList<String> = mutableListOf()
    }
}
