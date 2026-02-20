package com.originb.inkwisenote2.modules.noterelation.worker

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.common.util.CollectionUtils
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.ListUtils.listOf
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
import lombok.AllArgsConstructor
import org.greenrobot.eventbus.EventBus
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

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


    @AllArgsConstructor
    private inner class RelationInput {
        var bookId: Long = 0
        var noteId: Long = 0
    }

    override fun doWork(): Result {
        Try.to<T?>(Runnable {
            val bookId = getInputData().getLong("book_id", -1)
            val noteId = getInputData().getLong("note_id", -1)
            RelationInput(bookId, noteId)
        }, logger).get()
            .ifPresent(Consumer { relationInput: T? -> this.findRelatedNotesOfSmartBook(relationInput) })

        return Result.success()
    }

    fun findRelatedNotesOfSmartBook(relationInput: RelationInput) {
        val bookId = relationInput.bookId
        val noteId = relationInput.noteId
        logger.debug("Find related notes for bookId (new flow): " + bookId)

        val smartBookOpt = smartNotebookRepository.getSmartNotebooks(bookId)
        if (!smartBookOpt.isPresent()) {
            logger.error("SmartNotebook doesn't exists for bookId: " + bookId)
            return
        }

        val atomicNote = atomicNotesDomain.getAtomicNote(noteId)

        //        SmartNotebook smartNotebook = smartBookOpt.get();
//        logger.debug("Notebook bookId: " + bookId + " contains number of notes: " + smartNotebook.getAtomicNotes().size());
//        for (AtomicNoteEntity atomicNote : smartNotebook.getAtomicNotes()) {
        findRelatedNotes(bookId, atomicNote)

        //        }
        EventBus.getDefault().post(NoteStatus(bookId, TextProcessingStage.NOTE_READY))
    }

    fun findRelatedNotes(bookId: Long, noteEntity: AtomicNoteEntity) {
        val relatedNoteIds = getNoteIdsRelatedByTfIdf(noteEntity.noteId)
        relatedNoteIds.remove(noteEntity.noteId)
        if (relatedNoteIds.isEmpty()) {
            noteRelationDao.deleteByNoteId(noteEntity.noteId)
            return
        }

        val noteToBookMap: MutableMap<Long?, Long?> = smartBookPagesDao.getSmartBookPagesOfNote(relatedNoteIds).stream()
            .collect(
                (Collectors.toMap(
                    Function { obj: Function<in R?, out V?>? -> obj.getNoteId() },
                    Function { obj: Function<in R?, out V?>? -> obj.getBookId() }))
            )

        logger.debug("Related noteIds of bookId: " + bookId, listOf<Any?>(noteEntity, relatedNoteIds))

        val noteRelations = relatedNoteIds.stream()
            .filter { noteId: Long? -> noteId!! > 0 }
            .filter { key: Predicate<in T?>? -> noteToBookMap.containsKey(key) }
            .map<NoteRelation> { relatedNoteId: Long? ->
                NoteRelation(
                    noteEntity.noteId,
                    relatedNoteId,
                    bookId,
                    noteToBookMap.get(relatedNoteId),
                    TF_IDF_RELATION
                )
            }
            .collect(Collectors.toSet())

        noteRelationDao.deleteByNoteId(
            noteRelations.stream()
                .map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }
                .collect(Collectors.toList()))

        noteRelationDao.deleteByNoteId(
            noteRelations.stream()
                .map<Long?> { obj: Function<in R?, out V?>? -> obj.getRelatedNoteId() }
                .collect(Collectors.toList()))

        noteRelationDao.insertNoteRelatedNotes(noteRelations.toMutableSet())

        if (CollectionUtils.isEmpty(noteRelations)) return

        mainHandler.post(Runnable {
            // Code to be executed on the main thread
            AppState.updatedRelatedNotes(noteRelations)
        })
    }

    private fun getNoteIdsRelatedByTfIdf(noteId: Long): MutableSet<Long?> {
        val tfIdfScores = noteTfIdfLogic.getTfIdf(noteId)
        val filteredTerms: MutableSet<String?> = HashSet<String?>()
        for (key in tfIdfScores.keys) {
            if (tfIdfScores.get(key)!! > 0.1) {
                filteredTerms.add(key)
            }
        }

        val relatedNoteIds = noteTermFrequencyDao.getNoteIdsForTerms(filteredTerms)
            .stream().map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }
            .collect(Collectors.toSet())

        return relatedNoteIds
    }
}
