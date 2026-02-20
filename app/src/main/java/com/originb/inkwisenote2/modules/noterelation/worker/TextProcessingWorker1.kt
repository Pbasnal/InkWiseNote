package com.originb.inkwisenote2.modules.noterelation.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.common.util.CollectionUtils
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.common.Strings.isNumber
import com.originb.inkwisenote2.functionalUtils.Either
import com.originb.inkwisenote2.functionalUtils.Function2
import com.originb.inkwisenote2.functionalUtils.Try
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteStatus
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus.scheduleWorkForFindingRelatedNotesForBook
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.noterelation.service.NoteTfIdfLogic
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import lombok.AllArgsConstructor
import lombok.Getter
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate

class TextProcessingWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val noteTfIdfLogic: NoteTfIdfLogic,
    private val noteOcrTextDao: NoteOcrTextsDao,
    private val textNotesDao: TextNotesDao,
    private val atomicNotesDomain: AtomicNotesDomain,
    private val smartNotebookRepository: SmartNotebookRepository
) : Worker(context, workerParams) {
    private val logger = Logger("TextProcessingWorker")

    @AllArgsConstructor
    private inner class ProcessingInput {
        var bookId: Long = 0
        var noteId: Long = 0
    }

    override fun doWork(): Result {
        Try.to<T?>(Runnable {
            val bookId = getInputData().getLong("book_id", -1)
            val noteId = getInputData().getLong("note_id", -1)
            ProcessingInput(bookId, noteId)
        }, logger)
            .get()
            .filter(Predicate { processingInput: Predicate<in T?>? -> this.isNoteIdGreaterThan0(processingInput) })
            .ifPresent(Consumer { processingInput: T? -> this.processTextForNotebook(processingInput) })

        return Result.success()
    }

    private fun processTextForNotebook(processingInput: ProcessingInput) {
        val bookId = processingInput.bookId
        val noteId = processingInput.noteId
        logger.debug("Processing text of book (new flow). noteId: " + noteId)
        val smartBookOpt = smartNotebookRepository.getSmartNotebooks(bookId)
        if (!smartBookOpt.isPresent()) return
        val smartNotebook = smartBookOpt.get()

        val atomicNote = atomicNotesDomain.getAtomicNote(noteId)

        //        logger.debug("Notebook bookId: " + bookId + " contains number of notes: " + smartNotebook.getAtomicNotes().size());
//        for (AtomicNoteEntity atomicNote : smartNotebook.getAtomicNotes()) {
        val text: String?
        if (NoteType.HANDWRITTEN_PNG.toString() == atomicNote.noteType) {
            text = getHandwrittenNoteText(atomicNote)
        } else {
            text = getTextNoteText(atomicNote.noteId)
        }
        processTextForHandwrittenNote(atomicNote, text)

        //        }
        EventBus.getDefault().post(NoteStatus(bookId, TextProcessingStage.TOKENIZATION))
        scheduleWorkForFindingRelatedNotesForBook(getApplicationContext(), bookId, noteId)
    }

    private fun getTextNoteText(bookId: Long): String? {
        val textNoteEntity = textNotesDao.getTextNoteForNote(bookId)
        return textNoteEntity.noteText
    }

    private fun getHandwrittenNoteText(atomicNote: AtomicNoteEntity): String? {
        val noteOcrTexts = noteOcrTextDao.readTextFromDb(atomicNote.noteId)
        logger.debug("Ocr text of note: " + atomicNote.noteId, noteOcrTexts)

        return noteOcrTexts.extractedText
    }

    private fun processTextForHandwrittenNote(atomicNote: AtomicNoteEntity, text: String?) {
        val eitherTerms: Either<Exception?, DocumentTerms?> = handleException<Long?, String?, DocumentTerms?>(
            Function2 { noteId: Long?, text: String? -> this.extractTermsFromNote(noteId!!, text) },
            atomicNote.noteId, text
        )
        handleException<Long?, DocumentTerms?, Long?>(Function2 { noteId: Long?, documentTerms: DocumentTerms? ->
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

    private fun <A, B, R> handleException(
        function: Function2<A?, B?, R?>,
        input1: A?,
        input2: B?
    ): Either<Exception?, R?> {
        try {
            return Either.result(function.apply(input1, input2))
        } catch (ex: Exception) {
            Log.e("TextProcessingJob", "failed to process", ex)
            return Either.error(ex)
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
        documentTerms.terms = ArrayList<String?>()
        for (term in terms) {
            if (term.isEmpty()) continue
            if (isNumber(term)) continue

            documentTerms.terms!!.add(term)
        }
        logger.debug("Extracted document terms", documentTerms)
        return documentTerms
    }

    private fun createBiRelationalGraph(noteId: Long, documentTerms: DocumentTerms): Long? {
        if (Objects.isNull(documentTerms)) {
            logger.debug("empty document terms, skipping: " + noteId)
            return null
        }
        if (CollectionUtils.isEmpty(documentTerms.terms)) {
            logger.debug("terms list is empty, skipping: " + noteId)
            return documentTerms.documentId
        }
        noteTfIdfLogic.addOrUpdateNote(
            documentTerms.documentId,
            documentTerms.terms
        )

        return documentTerms.documentId
    }

    @Getter
    private class DocumentTerms {
        var documentId: Long? = null
        var terms: MutableList<String?>? = null
    }
}
