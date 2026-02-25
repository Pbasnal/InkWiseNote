package com.originb.inkwisenote2.modules.noterelation.worker

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.common.util.CollectionUtils
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.config.AppState
import com.originb.inkwisenote2.functionalUtils.Try
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteStatus
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.noterelation.service.NoteTfIdfLogic
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPagesDao
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable

class NoteRelationWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val noteTfIdfLogic: NoteTfIdfLogic,
    private val noteTermFrequencyDao: NoteTermFrequencyDao,
    private val noteRelationDao: NoteRelationDao,
    private val smartNotebookRepository: SmartNotebookRepository,
    private val smartBookPagesDao: SmartBookPagesDao,
    private val atomicNotesDomain: AtomicNotesDomain
) : Worker(context, workerParams) {
    private val mainHandler: Handler

    private val TF_IDF_RELATION = 1

    private val logger = Logger("NoteRelationWorker")

    init {
        this.mainHandler = Handler(Looper.getMainLooper())
    }


    private data class RelationInput(var bookId: Long = 0, var noteId: Long = 0)

    override fun doWork(): Result {
        val result = Try.to(
            Callable {
                val bookId = getInputData().getLong("book_id", -1)
                val noteId = getInputData().getLong("note_id", -1)
                RelationInput(bookId, noteId)
            },
            logger
        ).get()
        Optional.ofNullable(result)
            .filter { it.bookId != -1L && it.noteId != -1L }
            .ifPresent { findRelatedNotesOfSmartBook(it) }
        return Result.success()
    }

    private fun findRelatedNotesOfSmartBook(relationInput: RelationInput) {
        val bookId = relationInput.bookId
        val noteId = relationInput.noteId
        logger.debug("Find related notes for bookId (new flow): $bookId")

        if (smartNotebookRepository.getSmartNotebooks(bookId) == null) {
            logger.error("SmartNotebook doesn't exists for bookId: $bookId")
            return
        }

        val atomicNote = atomicNotesDomain.getAtomicNote(noteId)
        findRelatedNotes(bookId, atomicNote)
        EventBus.getDefault().post(NoteStatus(bookId, TextProcessingStage.NOTE_READY))
    }

    fun findRelatedNotes(bookId: Long, noteEntity: AtomicNoteEntity) {
        val relatedNoteIds = getNoteIdsRelatedByTfIdf(noteEntity.noteId)
        relatedNoteIds.remove(noteEntity.noteId)
        if (relatedNoteIds.isEmpty()) {
            noteRelationDao.deleteByNoteId(noteEntity.noteId)
            return
        }

        val pages = smartBookPagesDao.getSmartBookPagesOfNote(relatedNoteIds)
        val noteToBookMap: Map<Long, Long> = pages.associate { it.noteId to it.bookId }

        logger.debug("Related noteIds of bookId: $bookId", listOf(noteEntity, relatedNoteIds))

        val noteRelations = relatedNoteIds
            .filter { it > 0L && noteToBookMap.containsKey(it) }
            .mapNotNull { relatedNoteId ->
                val relatedBookId = noteToBookMap[relatedNoteId] ?: return@mapNotNull null
                NoteRelation(
                    noteEntity.noteId,
                    relatedNoteId,
                    bookId,
                    relatedBookId,
                    TF_IDF_RELATION
                )
            }
            .toMutableSet()

        val noteIdsToClear = mutableListOf(noteEntity.noteId)
        noteIdsToClear.addAll(relatedNoteIds)
        noteRelationDao.deleteByNoteId(noteIdsToClear)
        noteRelationDao.insertNoteRelatedNotes(noteRelations)

        if (CollectionUtils.isEmpty(noteRelations)) return

        mainHandler.post {
            AppState.updatedRelatedNotes(noteRelations)
        }
    }

    private fun getNoteIdsRelatedByTfIdf(noteId: Long): MutableSet<Long> {
        val tfIdfScores = noteTfIdfLogic.getTfIdf(noteId)
        val filteredTerms = tfIdfScores.keys.filter { (tfIdfScores[it] ?: 0.0) > 0.1 }.toMutableSet()

        return noteTermFrequencyDao.getNoteIdsForTerms(filteredTerms)
            .map { it.noteId }
            .toMutableSet()
    }
}
