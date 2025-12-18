package com.originb.inkwisenote2.modules.notesearch;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import lombok.NonNull;

public class NoteSearchViewModelFactory implements ViewModelProvider.Factory {
    private final NoteOcrTextDao ocrDao;
    private final SmartNotebookRepository repo;

    public NoteSearchViewModelFactory(NoteOcrTextDao ocrDao, SmartNotebookRepository repo) {
        this.ocrDao = ocrDao;
        this.repo = repo;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new NoteSearchViewModel(ocrDao, repo);
    }
}