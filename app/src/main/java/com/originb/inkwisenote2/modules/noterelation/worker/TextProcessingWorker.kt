package com.originb.inkwisenote2.modules.noterelation.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.android.gms.common.util.CollectionUtils
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.functionalUtils.Either
import com.originb.inkwisenote2.functionalUtils.Function2
import com.originb.inkwisenote2.functionalUtils.Try
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteStatus
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.noterelation.service.NoteTfIdfLogic
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import lombok.Getter
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer
import java.util.function.Predicate

class TextProcessingWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val noteTfIdfLogic = NoteTfIdfLogic(Repositories.Companion.getInstance())
    private val noteOcrTextDao: NoteOcrTextDao =
        Repositories.Companion.getInstance().getNotesDb().noteOcrTextDao()

    private val textNotesDao: TextNotesDao =
        Repositories.Companion.getInstance().getNotesDb().textNotesDao()

    private val logger = Logger("TextProcessingWorker")

    private val smartNotebookRepository: SmartNotebookRepository =
        Repositories.Companion.getInstance().getSmartNotebookRepository()

    override fun doWork(): Result {
        Try.Companion.to<Long>(Callable<Long> { inputData.getLong("book_id", -1) }, logger)
            .get()
            .filter(Predicate<Long> { noteId: Long -> this.isNoteIdGreaterThan0(noteId) })
            .ifPresent(Consumer<Long> { bookId: Long -> this.processTextForNotebook(bookId) })

        return Result.success()
    }

    private fun processTextForNotebook(bookId: Long) {
        logger.debug("Processing text of book (new flow). noteId: $bookId")
        val smartBookOpt = smartNotebookRepository.getSmartNotebooks(bookId)
        if (!smartBookOpt!!.isPresent) return
        val smartNotebook = smartBookOpt.get()

        EventBus.getDefault().post(NoteStatus(smartNotebook, TextProcessingStage.TOKENIZATION))
        logger.debug("Notebook bookId: " + bookId + " contains number of notes: " + smartNotebook.getAtomicNotes().size)
        for (atomicNote in smartNotebook.getAtomicNotes()) {
            var text = if (NoteType.HANDWRITTEN_PNG.toString() == atomicNote.noteType) {
                getHandwrittenNoteText(atomicNote)
            } else {
                getTextNoteText(bookId)
            }
            processTextForHandwrittenNote(atomicNote, text)
        }

        WorkManagerBus.scheduleWorkForFindingRelatedNotesForBook(applicationContext, bookId)
    }

    private fun getTextNoteText(bookId: Long): String {
        val textNoteEntity = textNotesDao.getTextNoteForBook(bookId)
        return textNoteEntity.noteText
    }

    private fun getHandwrittenNoteText(atomicNote: AtomicNoteEntity): String {
        val noteOcrTexts = noteOcrTextDao.readTextFromDb(atomicNote.noteId)
        logger.debug("Ocr text of note: " + atomicNote.noteId, noteOcrTexts)

        return noteOcrTexts.extractedText
    }

    private fun processTextForHandwrittenNote(atomicNote: AtomicNoteEntity, text: String) {
        val eitherTerms = handleException<Long, String, DocumentTerms>(
            Function2<Long, String, DocumentTerms> { noteId: A?, text: B? -> this.extractTermsFromNote(noteId, text) },
            atomicNote.noteId, text
        )
        handleException<Long, DocumentTerms?, Long>(Function2<Long, DocumentTerms?, Long> { noteId: A?, documentTerms: B? ->
            createBiRelationalGraph(
                noteId,
                documentTerms
            )!!
        }, atomicNote.noteId, eitherTerms.result)
    }

    private fun isNoteIdGreaterThan0(noteId: Long): Boolean {
        if (noteId == -1L) {
            logger.error("Got incorrect note id (-1) as input")
            return false
        }
        return true
    }

    private fun <A, B, R> handleException(function: Function2<A, B, R>, input1: A, input2: B): Either<Exception, R> {
        try {
            return Either.Companion.result<R?>(function.apply(input1, input2))
        } catch (ex: Exception) {
            Log.e("TextProcessingJob", "failed to process", ex)
            return Either.Companion.error<Exception>(ex)
        }
    }

    private fun extractTermsFromNote(noteId: Long, text: String?): DocumentTerms {
        var text = text
        val documentTerms = DocumentTerms()
        documentTerms.documentId = noteId

        if (text == null || text.isEmpty()) {
            logger.debug("Note has empty text, skipping: $noteId", text)
            return documentTerms // Return an empty list for null or empty text
        }

        // Step 1: Normalize the text to lowercase
        text = text.lowercase(Locale.getDefault())

        // Step 2: Remove non-alphanumeric characters except spaces
        text = text.replace("[^a-z0-9\\s]".toRegex(), "")

        // Step 3: Split the text into terms by whitespace
        val terms = text.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        documentTerms.terms = ArrayList()
        for (term in terms) {
            if (term.isEmpty()) continue
            if (Strings.isNumber(term)) continue

            documentTerms.terms.add(term)
        }
        logger.debug("Extracted document terms", documentTerms)
        return documentTerms
    }

    private fun createBiRelationalGraph(noteId: Long, documentTerms: DocumentTerms?): Long? {
        if (Objects.isNull(documentTerms)) {
            logger.debug("empty document terms, skipping: $noteId")
            return null
        }
        if (CollectionUtils.isEmpty(documentTerms!!.terms)) {
            logger.debug("terms list is empty, skipping: $noteId")
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
