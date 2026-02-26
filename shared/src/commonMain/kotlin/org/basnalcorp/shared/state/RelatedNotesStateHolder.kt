package org.basnalcorp.shared.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.basnalcorp.shared.data.repository.SmartNotebookRepository
import org.basnalcorp.shared.db.NotesDatabase
import org.basnalcorp.shared.domain.SmartNotebook

/**
 * State holder for RelatedNotesScreen (Phase 5).
 * Loads root notebook and related notebooks (via note_relation) for a given [bookId].
 * Call [load] when the route appears (e.g. LaunchedEffect(bookId) { load(bookId) }).
 */
class RelatedNotesStateHolder(
    private val db: NotesDatabase,
    private val smartNotebookRepository: SmartNotebookRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _rootNotebook = MutableStateFlow<SmartNotebook?>(null)
    val rootNotebook: StateFlow<SmartNotebook?> = _rootNotebook.asStateFlow()
    private val _relatedNotebooks = MutableStateFlow<List<SmartNotebook>>(emptyList())
    val relatedNotebooks: StateFlow<List<SmartNotebook>> = _relatedNotebooks.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load(bookId: Long) {
        scope.launch {
            _isLoading.value = true
            val root = smartNotebookRepository.getByBookId(bookId)
            _rootNotebook.value = root
            val related = if (root == null) emptyList() else loadRelatedNotebooks(bookId, root)
            _relatedNotebooks.value = related
            _isLoading.value = false
        }
    }

    private fun loadRelatedNotebooks(rootBookId: Long, root: SmartNotebook): List<SmartNotebook> {
        val noteIds = root.atomicNotes.map { it.noteId }
        val relatedBookIds = mutableSetOf<Long>()
        for (noteId in noteIds) {
            db.noteRelationQueries.getNoteRelationsForNote(noteId).executeAsList().forEach { rel ->
                relatedBookIds.add(rel.book_id)
                relatedBookIds.add(rel.related_book_id)
            }
        }
        relatedBookIds.remove(rootBookId)
        return relatedBookIds.mapNotNull { smartNotebookRepository.getByBookId(it) }
    }
}
