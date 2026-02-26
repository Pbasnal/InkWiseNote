package org.basnalcorp.shared.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.basnalcorp.shared.data.repository.SmartNotebookRepository
import org.basnalcorp.shared.domain.SmartNotebook
import org.basnalcorp.shared.util.isNullOrWhitespace

/**
 * State holder for SmartNotebookScreen (Phase 1).
 * Loads a notebook by bookId or creates a new one when bookId is null and workingPath is set.
 * Call [load] when the route changes (e.g. LaunchedEffect(route) { stateHolder.load(...) }).
 */
class SmartNotebookStateHolder(
    private val smartNotebookRepository: SmartNotebookRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _notebook = MutableStateFlow<SmartNotebook?>(null)
    val notebook: StateFlow<SmartNotebook?> = _notebook.asStateFlow()

    /** When set, SmartNotebookScreen should scroll the pager to this note (survives navigation). */
    private val _selectedNoteId = MutableStateFlow<Long?>(null)
    val selectedNoteId: StateFlow<Long?> = _selectedNoteId.asStateFlow()

    fun setSelectedNoteId(noteId: Long?) {
        _selectedNoteId.value = noteId
    }

    fun load(
        bookId: Long?,
        workingPath: String?,
        bookTitle: String?,
        selectedNoteId: Long?
    ) {
        scope.launch {
            val notebook = when {
                bookId != null && bookId != -1L -> smartNotebookRepository.getByBookId(bookId)
                !isNullOrWhitespace(workingPath) -> smartNotebookRepository.createNotebook(
                    title = bookTitle?.takeIf { !isNullOrWhitespace(it) } ?: "New",
                    directoryPath = workingPath!!,
                    noteType = "not_set"
                )
                else -> null
            }
            _notebook.value = notebook
        }
    }

    /**
     * Creates a new notebook with the first note already set to the given type (e.g. "text_note" or "handwritten_png").
     * Updates [notebook] when done; the UI can observe and then navigate to SmartNotebook + NoteDetail.
     */
    fun createNotebookWithFirstNote(title: String, workingPath: String, noteType: String) {
        scope.launch {
            val notebook = smartNotebookRepository.createNotebook(
                title = title.trim(),
                directoryPath = workingPath,
                noteType = noteType
            )
            _notebook.value = notebook
        }
    }

    fun addPage() {
        val current = _notebook.value ?: return
        scope.launch {
            val updated = smartNotebookRepository.addPage(current)
            _notebook.value = updated
        }
    }

    /**
     * Adds a new note to the notebook with the given type ("text_note" or "handwritten_png").
     * Updates [notebook]; UI can observe and navigate to the new (last) note.
     */
    fun addPageWithType(noteType: String) {
        val current = _notebook.value ?: return
        scope.launch {
            val updated = smartNotebookRepository.addPage(current, noteType)
            _notebook.value = updated
        }
    }

    fun deleteNotebook(notebook: SmartNotebook) {
        scope.launch {
            smartNotebookRepository.deleteNotebook(notebook)
            _notebook.value = null
        }
    }

    fun updateNotebook(notebook: SmartNotebook) {
        scope.launch {
            smartNotebookRepository.updateNotebook(notebook)
            _notebook.value = notebook
        }
    }
}
