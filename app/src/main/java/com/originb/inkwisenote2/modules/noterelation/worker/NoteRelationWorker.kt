package com.originb.inkwisenote2.modules.noterelation.worker

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.*
import com.google.android.gms.common.util.CollectionUtils
import com.originb.inkwisenote2.common.ListUtils
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.config.AppState
import com.originb.inkwisenote2.functionalUtils.Try
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteStatus
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.noterelation.service.NoteTfIdfLogic
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPage
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPagesDao
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.Callable
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

class NoteRelationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val noteTfIdfLogic = NoteTfIdfLogic(Repositories.Companion.getInstance())
    private val noteTermFrequencyDao: NoteTermFrequencyDao =
        Repositories.Companion.getInstance().getNotesDb().noteTermFrequencyDao()
    private val noteRelationDao: NoteRelationDao =
        Repositories.Companion.getInstance().getNotesDb().noteRelationDao()
    private val smartNotebookRepository: SmartNotebookRepository =
        Repositories.Companion.getInstance().getSmartNotebookRepository()
    private val smartBookPagesDao: SmartBookPagesDao =
        Repositories.Companion.getInstance().getNotesDb().smartBookPagesDao()

    private val mainHandler = Handler(Looper.getMainLooper())

    private val TF_IDF_RELATION = 1

    private val logger = Logger("NoteRelationWorker")


    override fun doWork(): Result {
        Try.Companion.to<Long>(Callable<Long> { inputData.getLong("book_id", -1) }, logger).get()
            .ifPresent(Consumer<Long> { bookId: Long -> this.findRelatedNotesOfSmartBook(bookId) })

        return Result.success()
    }

    fun findRelatedNotesOfSmartBook(bookId: Long) {
        logger.debug("Find related notes for bookId (new flow): $bookId")

        val smartBookOpt = smartNotebookRepository.getSmartNotebooks(bookId)
        if (!smartBookOpt!!.isPresent) {
            logger.error("SmartNotebook doesn't exists for bookId: $bookId")
            return
        }

        val smartNotebook = smartBookOpt.get()
        logger.debug("Notebook bookId: " + bookId + " contains number of notes: " + smartNotebook.getAtomicNotes().size)
        for (atomicNote in smartNotebook.getAtomicNotes()) {
            findRelatedNotes(smartNotebook.getSmartBook().bookId, atomicNote)
        }

        EventBus.getDefault().post(NoteStatus(smartNotebook, TextProcessingStage.NOTE_READY))
    }

    fun findRelatedNotes(bookId: Long, noteEntity: AtomicNoteEntity) {
        val relatedNoteIds = getNoteIdsRelatedByTfIdf(noteEntity.noteId)
        relatedNoteIds.remove(noteEntity.noteId)
        if (relatedNoteIds.isEmpty()) {
            noteRelationDao.deleteByNoteId(noteEntity.noteId)
            return
        }

        val noteToBookMap = smartBookPagesDao.getSmartBookPagesOfNote(relatedNoteIds).stream()
            .collect(
                (Collectors.toMap(
                    Function { obj: SmartBookPage? -> obj.getNoteId() },
                    Function { obj: SmartBookPage? -> obj.getBookId() }))
            )

        logger.debug("Related noteIds of bookId: $bookId", ListUtils.listOf(noteEntity, relatedNoteIds))

        val noteRelations = relatedNoteIds.stream()
            .filter { noteId: Long -> noteId > 0 }
            .filter { key: Long -> noteToBookMap.containsKey(key) }
            .map { relatedNoteId: Long ->
                NoteRelation(
                    noteEntity.noteId,
                    relatedNoteId,
                    bookId,
                    noteToBookMap[relatedNoteId],
                    TF_IDF_RELATION
                )
            }
            .collect(Collectors.toSet())

        noteRelationDao.deleteByNoteId(noteRelations.stream()
            .map { obj: NoteRelation? -> obj.getNoteId() }
            .collect(Collectors.toList()))

        noteRelationDao.deleteByNoteId(noteRelations.stream()
            .map { obj: NoteRelation? -> obj.getRelatedNoteId() }
            .collect(Collectors.toList()))

        noteRelationDao.insertNoteRelatedNotes(noteRelations)

        if (CollectionUtils.isEmpty(noteRelations)) return

        mainHandler.post {
            // Code to be executed on the main thread
            AppState.Companion.updatedRelatedNotes(noteRelations)
        }
    }

    private fun getNoteIdsRelatedByTfIdf(noteId: Long): MutableSet<Long> {
        val tfIdfScores = noteTfIdfLogic.getTfIdf(noteId)
        val filteredTerms: MutableSet<String?> = HashSet()
        for (key in tfIdfScores!!.keys) {
            if (tfIdfScores[key]!! > 0.1) {
                filteredTerms.add(key)
            }
        }

        val relatedNoteIds = noteTermFrequencyDao.getNoteIdsForTerms(filteredTerms)
            .stream().map { obj: NoteTermFrequency? -> obj.getNoteId() }
            .collect(Collectors.toSet())

        return relatedNoteIds
    }
}
