package org.basnalcorp.shared.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.basnalcorp.shared.data.repository.AtomicNotesRepository
import org.basnalcorp.shared.data.repository.TextNotesRepository
import org.basnalcorp.shared.domain.AtomicNote
import org.basnalcorp.shared.domain.TextNote
import org.basnalcorp.shared.writeTextFile
import kotlinx.datetime.Clock

/**
 * State holder for NoteDetailScreen (Phase 2).
 * Load(bookId, noteId, isHandwritten): loads AtomicNote and text content if text note.
 * When note type is "not_set", UI shows init flow (choose Text / Handwritten).
 */
class NoteDetailStateHolder(
    private val atomicNotesRepository: AtomicNotesRepository,
    private val textNotesRepository: TextNotesRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _note = MutableStateFlow<AtomicNote?>(null)
    val note: StateFlow<AtomicNote?> = _note.asStateFlow()
    private val _textContent = MutableStateFlow<String>("")
    val textContent: StateFlow<String> = _textContent.asStateFlow()

    fun load(bookId: Long, noteId: Long) {
        scope.launch {
            val atomicNote = atomicNotesRepository.get(noteId)
            _note.value = atomicNote
            if (atomicNote != null && atomicNote.noteType == "text_note") {
                var textNote = textNotesRepository.getForNote(noteId)
                if (textNote == null) {
                    textNotesRepository.insertOrReplace(
                        TextNote(
                            noteId = noteId,
                            bookId = bookId,
                            noteText = "",
                            createdTimeMillis = atomicNote.createdTimeMillis,
                            lastModifiedTimeMillis = atomicNote.lastModifiedTimeMillis
                        )
                    )
                    textNote = textNotesRepository.getForNote(noteId)
                }
                _textContent.value = textNote?.noteText ?: ""
            } else {
                _textContent.value = ""
            }
        }
    }

    /** True when the note exists and its type is not_set (show init UI). */
    val isInitMode: Boolean
        get() = _note.value?.noteType == "not_set" || _note.value?.noteType.isNullOrBlank()

    fun setNoteTypeToText(bookId: Long) {
        val n = _note.value ?: return
        scope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val filename = if (n.filename.isNullOrBlank()) now.toString() else n.filename
            val updated = n.copy(
                noteType = "text_note",
                filename = filename,
                lastModifiedTimeMillis = now
            )
            atomicNotesRepository.update(updated)
            textNotesRepository.insertOrReplace(
                TextNote(
                    noteId = n.noteId,
                    bookId = bookId,
                    noteText = "",
                    createdTimeMillis = n.createdTimeMillis,
                    lastModifiedTimeMillis = now
                )
            )
            val path = updated.filepath
            if (!path.isNullOrBlank() && !filename.isNullOrBlank()) {
                try {
                    writeTextFile("$path/$filename.md", "")
                } catch (_: Exception) { }
            }
            _note.value = updated
            _textContent.value = ""
        }
    }

    fun setNoteTypeToHandwritten() {
        val n = _note.value ?: return
        scope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val updated = n.copy(
                noteType = "handwritten_png",
                lastModifiedTimeMillis = now
            )
            atomicNotesRepository.update(updated)
            _note.value = updated
        }
    }

    fun saveText(content: String) {
        val n = _note.value ?: return
        if (n.noteType != "text_note") return
        scope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val updated = n.copy(lastModifiedTimeMillis = now)
            atomicNotesRepository.update(updated)
            _note.value = updated
            textNotesRepository.updateText(n.noteId, content, now)
            _textContent.value = content
            val path = n.filepath
            val name = n.filename
            if (!path.isNullOrBlank() && !name.isNullOrBlank()) {
                try {
                    writeTextFile("$path/$name.md", content)
                } catch (_: Exception) { }
            }
        }
    }

    fun updateTextContentLocally(content: String) {
        _textContent.value = content
    }
}
