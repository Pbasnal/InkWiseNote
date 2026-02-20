package com.originb.inkwisenote2.modules.noterelation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.BitmapScale
import com.originb.inkwisenote2.functionalUtils.Try.get
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao
import com.originb.inkwisenote2.modules.noterelation.data.RelatedNotesUiState
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

class RelatedNotesViewModel(
    private val smartNotebookRepository: SmartNotebookRepository,
    private val handwrittenNoteRepository: HandwrittenNoteRepository,
    private val noteRelationRepository: NoteRelationRepository,
    private val noteRelationDao: NoteRelationDao
) : ViewModel() {
    private val _uiState = MutableLiveData<RelatedNotesUiState?>()
    val uiState: LiveData<RelatedNotesUiState?> = _uiState

    private val _noteDeletedEvent = MutableLiveData<Boolean?>(false)
    val noteDeletedEvent: LiveData<Boolean?> = _noteDeletedEvent

    fun loadRelatedNotes(rootBookId: Long) {
        execute(Runnable {
            val rootOpt = smartNotebookRepository.getSmartNotebooks(rootBookId)
            if (!rootOpt.isPresent()) return@execute null

            val rootBook = rootOpt.get()
            val noteIds = rootBook.atomicNotes.stream()
                .map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }
                .collect(Collectors.toSet())

            val noteRelations = noteRelationDao.getRelatedNotesOf(noteIds)

            val allBookIds = noteRelations.stream()
                .map<Long?> { obj: Function<in R?, out V?>? -> obj.getBookId() }.collect(Collectors.toSet())
            allBookIds.addAll(
                noteRelations.stream()
                    .map<Long?> { obj: Function<in R?, out V?>? -> obj.getRelatedBookId() }.collect(Collectors.toSet())
            )

            allBookIds.remove(rootBook.getSmartBook().getBookId())

            val relatedBooks = allBookIds.stream()
                .map<Optional<SmartNotebook?>?> { bookId: Function<in R?, out V?>? ->
                    smartNotebookRepository.getSmartNotebooks(
                        bookId
                    )
                }
                .filter { obj: Predicate<in T?>? -> obj.isPresent() }
                .map<SmartNotebook?> { obj: Function<in R?, out V?>? -> obj.get() }
                .collect(Collectors.toList())

            val firstNote = rootBook.getAtomicNotes().get(0)
            val image = handwrittenNoteRepository.getNoteImage(firstNote, BitmapScale.THUMBNAIL)
            RelatedNotesUiState(rootBook, image, HashSet<NoteRelation?>(noteRelations), relatedBooks)
        }, Runnable { state ->
            if (state != null) _uiState.setValue(state)
        })
    }

    fun deleteRootNote(notebook: SmartNotebook) {
        execute(Runnable {
            notebook.atomicNotes.forEach(Consumer { note: AtomicNoteEntity? ->
                handwrittenNoteRepository.deleteHandwrittenNote(note!!)
                noteRelationRepository.deleteNoteRelationData(note)
            })
            smartNotebookRepository.deleteSmartNotebook(notebook)
            true
        }, Runnable { result -> _noteDeletedEvent.setValue(true) })
    }
}