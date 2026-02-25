package com.originb.inkwisenote2.modules.noterelation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.originb.inkwisenote2.common.BitmapScale
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao
import com.originb.inkwisenote2.modules.noterelation.data.RelatedNotesUiState
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import java.util.concurrent.Callable
import java.util.function.Consumer

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
        BackgroundOps.execute(
            Callable {
                val rootBook = smartNotebookRepository.getSmartNotebooks(rootBookId) ?: return@Callable null
                val noteIds = rootBook.atomicNotes.map { it.noteId }.toMutableSet()
                val noteRelations = noteRelationDao.getRelatedNotesOf(noteIds)

                val allBookIds = (noteRelations.map { it.bookId } + noteRelations.map { it.relatedBookId }).toMutableSet()
                rootBook.smartBook?.bookId?.let { allBookIds.remove(it) }

                val relatedBooks = allBookIds.mapNotNull { bookId ->
                    smartNotebookRepository.getSmartNotebooks(bookId)
                }.toMutableList()

                val firstNote = rootBook.atomicNotes.getOrNull(0)
                val image = firstNote?.let { handwrittenNoteRepository.getNoteImage(it, BitmapScale.THUMBNAIL) }
                RelatedNotesUiState(rootBook, image, noteRelations.toMutableSet(), relatedBooks)
            },
            Consumer { state ->
                if (state != null) _uiState.setValue(state)
            }
        )
    }

    fun deleteRootNote(notebook: SmartNotebook) {
        BackgroundOps.execute(
            Callable {
                notebook.atomicNotes.forEach { note ->
                    handwrittenNoteRepository.deleteHandwrittenNote(note)
                    noteRelationRepository.deleteNoteRelationData(note)
                }
                smartNotebookRepository.deleteSmartNotebook(notebook)
                true
            },
            Consumer { _noteDeletedEvent.setValue(true) }
        )
    }
}