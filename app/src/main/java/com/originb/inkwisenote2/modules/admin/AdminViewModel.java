package com.originb.inkwisenote2.modules.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNotesDao;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao;
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntitiesDao;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPagesDao;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBooksDao;

import java.io.File;
import java.util.Arrays;

public class AdminViewModel extends ViewModel {

    // DAOs
    private final NoteTermFrequencyDao termFreqDao;
    private final NoteOcrTextsDao ocrDao;
    private final AtomicNoteEntitiesDao atomicDao;
    private final SmartBooksDao booksDao;
    private final SmartBookPagesDao pagesDao;
    private final HandwrittenNotesDao handNotesDao;

    // Observable Data
    private final MutableLiveData<AdminUiState> _uiState = new MutableLiveData<>();
    public LiveData<AdminUiState> uiState = _uiState;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;

    private File currentDirectory;

    public AdminViewModel(
            NoteTermFrequencyDao termFreqDao,
            NoteOcrTextsDao ocrDao,
            AtomicNoteEntitiesDao atomicDao,
            SmartBooksDao booksDao,
            SmartBookPagesDao pagesDao,
            HandwrittenNotesDao handNotesDao) {

        this.termFreqDao = termFreqDao;
        this.ocrDao = ocrDao;
        this.atomicDao = atomicDao;
        this.booksDao = booksDao;
        this.pagesDao = pagesDao;
        this.handNotesDao = handNotesDao;
    }

    public void loadData(String tabName, Long noteId, File defaultDir) {
        if (currentDirectory == null) currentDirectory = defaultDir;

        BackgroundOps.execute(() -> {
            switch (tabName) {
                case "Term Frequencies":
                    return new AdminUiState.DataList(termFreqDao.getAllTermFrequencies(), tabName);
                case "Note Text":
                    return new AdminUiState.DataList(ocrDao.getAllNoteText(), tabName);
                case "Atomic Notes":
                    return new AdminUiState.DataList(atomicDao.getAllAtomicNotes(), tabName);
                case "Smart Books":
                    return new AdminUiState.DataList(booksDao.getAllSmartBooks(), tabName);
                case "Smart Book Pages":
                    return new AdminUiState.DataList(pagesDao.getAllSmartBookPages(), tabName);
                case "Handwritten Notes":
                    return new AdminUiState.DataList(handNotesDao.getAllHandwrittenNotes(), tabName);
                case "Files":
                    return getFilesState(currentDirectory);
                default:
                    return null;
            }
        }, result -> {
            if (result != null) _uiState.setValue((AdminUiState) result);
        });
    }

    private AdminUiState.FilesState getFilesState(File directory) {
        this.currentDirectory = directory;
        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.sort(files, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            });
        }
        return new AdminUiState.FilesState(directory, files != null ? Arrays.asList(files) : null);
    }

    public void navigateToDir(File directory) {
        loadData("Files", 0L, directory);
    }

    public void deleteFile(File file) {
        if (file.isDirectory() && file.listFiles() != null && file.listFiles().length > 0) {
            _toastMessage.setValue("Cannot delete non-empty directory");
            return;
        }
        if (file.delete()) {
            _toastMessage.setValue("Deleted: " + file.getName());
            navigateToDir(currentDirectory);
        } else {
            _toastMessage.setValue("Failed to delete: " + file.getName());
        }
    }
}
