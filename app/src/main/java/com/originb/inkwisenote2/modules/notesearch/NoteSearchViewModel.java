package com.originb.inkwisenote2.modules.notesearch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NoteSearchViewModel extends ViewModel {
    private final NoteOcrTextDao noteOcrTextDao;
    private final SmartNotebookRepository smartNotebookRepository;

    // Observable data for the UI
    private final MutableLiveData<List<SmartNotebook>> _searchResults = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<SmartNotebook>> searchResults = _searchResults;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;

    // Constructor (Dependencies should be passed here)
    public NoteSearchViewModel(NoteOcrTextDao ocrDao, SmartNotebookRepository notebookRepo) {
        this.noteOcrTextDao = ocrDao;
        this.smartNotebookRepository = notebookRepo;
    }

    public void performSearch(String query) {
        // Validation Logic
        if (query == null || query.trim().length() < 3) {
            _toastMessage.setValue("Enter at least 3 characters to search");
            return;
        }

        // Execution Logic
        BackgroundOps.execute(() -> {
                    // 1. Fetch notebooks matching query name
                    Set<SmartNotebook> combinedResults = smartNotebookRepository.getSmartNotebooks(query);

                    // 2. Fetch notebooks matching OCR text
                    List<NoteOcrText> noteOcrs = noteOcrTextDao.searchTextFromDb(query);
                    if (noteOcrs != null && !noteOcrs.isEmpty()) {
                        Set<Long> noteIds = noteOcrs.stream()
                                .map(NoteOcrText::getNoteId)
                                .collect(Collectors.toSet());

                        combinedResults.addAll(smartNotebookRepository.getSmartNotebooksForNoteIds(noteIds));
                    }

                    return new ArrayList<>(combinedResults);
                },
                results -> {
                    // Update the LiveData on the Main Thread
                    _searchResults.setValue(results);
                });
    }
}
