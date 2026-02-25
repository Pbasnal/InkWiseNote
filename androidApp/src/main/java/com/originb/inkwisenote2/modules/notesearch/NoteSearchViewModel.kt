package com.originb.inkwisenote2.modules.notesearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import java.util.concurrent.Callable
import java.util.function.Consumer

class NoteSearchViewModel // Constructor (Dependencies should be passed here)
    (private val noteOcrTextDao: NoteOcrTextsDao, private val smartNotebookRepository: SmartNotebookRepository) :
    ViewModel() {
    // Observable data for the UI
    private val _searchResults = MutableLiveData<MutableList<SmartNotebook>>(ArrayList<SmartNotebook>())
    var searchResults: LiveData<MutableList<SmartNotebook>> = _searchResults

    private val _toastMessage = MutableLiveData<String?>()
    var toastMessage: LiveData<String?> = _toastMessage

    fun performSearch(query: String?) {
        // Validation Logic
        if (query == null || query.trim { it <= ' ' }.length < 3) {
            _toastMessage.value = "Enter at least 3 characters to search"
            return
        }

        val searchQuery = query.trim()
        BackgroundOps.execute(
            { getCombinedResults(searchQuery) },
            { results ->
                _searchResults.value = results
            }
        )
    }

    fun getCombinedResults(searchQuery: String): MutableList<SmartNotebook> {
        val combinedResults = smartNotebookRepository.getSmartNotebooks(searchQuery).toMutableSet()

        // 2. Fetch notebooks matching OCR text
        val noteOcrs = noteOcrTextDao.searchTextFromDb(searchQuery)
        if (noteOcrs.isNotEmpty()) {
            val noteIds = noteOcrs.map { it.noteId }.toMutableSet()
            combinedResults.addAll(smartNotebookRepository.getSmartNotebooksForNoteIds(noteIds))
        }
        return combinedResults.toMutableList()
    }
}
