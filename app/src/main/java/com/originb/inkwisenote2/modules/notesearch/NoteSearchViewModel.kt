package com.originb.inkwisenote2.modules.notesearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import java.util.function.Function
import java.util.stream.Collectors

class NoteSearchViewModel // Constructor (Dependencies should be passed here)
    (private val noteOcrTextDao: NoteOcrTextsDao, private val smartNotebookRepository: SmartNotebookRepository) :
    ViewModel() {
    // Observable data for the UI
    private val _searchResults = MutableLiveData<MutableList<SmartNotebook?>?>(ArrayList<SmartNotebook?>())
    var searchResults: LiveData<MutableList<SmartNotebook?>?> = _searchResults

    private val _toastMessage = MutableLiveData<String?>()
    var toastMessage: LiveData<String?> = _toastMessage

    fun performSearch(query: String?) {
        // Validation Logic
        if (query == null || query.trim { it <= ' ' }.length < 3) {
            _toastMessage.setValue("Enter at least 3 characters to search")
            return
        }

        // Execution Logic
        execute(
            Runnable {
                // 1. Fetch notebooks matching query name
                val combinedResults = smartNotebookRepository.getSmartNotebooks(query)

                // 2. Fetch notebooks matching OCR text
                val noteOcrs = noteOcrTextDao.searchTextFromDb(query)
                if (noteOcrs != null && !noteOcrs.isEmpty()) {
                    val noteIds = noteOcrs.stream()
                        .map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }
                        .collect(Collectors.toSet())

                    combinedResults.addAll(smartNotebookRepository.getSmartNotebooksForNoteIds(noteIds))
                }
                ArrayList<SmartNotebook?>(combinedResults)
            },
            Runnable { results ->
                // Update the LiveData on the Main Thread
                _searchResults.setValue(results)
            })
    }
}
