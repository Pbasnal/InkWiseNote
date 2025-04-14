package com.originb.inkwisenote2.modules.smartnotes.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPage;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SmartNotebookViewModel extends ViewModel {
    private final SmartNotebookRepository smartNotebookRepository;
    private final AtomicNotesDomain atomicNotesDomain;
    
    private final MutableLiveData<SmartNotebook> smartNotebook = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPageIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalPages = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> showNextButton = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> showPrevButton = new MutableLiveData<>(false);
    
    private String workingNotePath;
    
    public SmartNotebookViewModel() {
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        this.atomicNotesDomain = Repositories.getInstance().getAtomicNotesDomain();
    }
    
    public void loadSmartNotebook(Long bookId, String workingNotePath, String noteIdsString) {
        this.workingNotePath = workingNotePath;
        
        BackgroundOps.executeOpt(() -> {
            if (bookId != null && bookId != -1) {
                return smartNotebookRepository.getSmartNotebooks(bookId);
            } else if (noteIdsString != null && !noteIdsString.isEmpty()) {
                Set<Long> noteIdsSet = Arrays.stream(noteIdsString.split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());
                return smartNotebookRepository.getVirtualSmartNotebooks(noteIdsSet);
            }
            
            return smartNotebookRepository.initializeNewSmartNotebook("",
                    workingNotePath,
                    NoteType.NOT_SET);
        }, notebook -> {
            smartNotebook.setValue(notebook);
            updatePageCounters(notebook, currentPageIndex.getValue());
        });
    }
    
    public void saveNoteTitle(String title) {
        SmartNotebook notebook = smartNotebook.getValue();
        if (notebook != null) {
            BackgroundOps.execute(() -> {
                notebook.smartBook.setTitle(title);
                smartNotebookRepository.saveSmartNotebook(notebook);
                return notebook;
            }, updatedNotebook -> {
                smartNotebook.setValue(updatedNotebook);
            });
        }
    }
    
    public void addNewPage() {
        SmartNotebook notebook = smartNotebook.getValue();
        Integer index = currentPageIndex.getValue();
        
        if (notebook != null && index != null) {
            int positionToInsert = index + 1;
            
            BackgroundOps.execute(() -> {
                AtomicNoteEntity newAtomicNote = atomicNotesDomain.saveAtomicNote(
                        AtomicNotesDomain.constructAtomicNote(
                                "",
                                workingNotePath,
                                NoteType.NOT_SET));
                
                SmartBookPage newSmartPage = smartNotebookRepository.newSmartBookPage(
                        notebook.smartBook,
                        newAtomicNote, 
                        positionToInsert - 1);
                
                notebook.insertAtomicNoteAndPage(positionToInsert - 1, newAtomicNote, newSmartPage);
                
                return notebook;
            }, updatedNotebook -> {
                smartNotebook.setValue(updatedNotebook);
                setCurrentPageIndex(positionToInsert);
            });
        }
    }
    
    public void saveCurrentPage(AtomicNoteEntity note) {
        SmartNotebook notebook = smartNotebook.getValue();
        Integer index = currentPageIndex.getValue();
        
        if (notebook != null && index != null && index < notebook.getAtomicNotes().size()) {
            BackgroundOps.execute(() -> {
                // Save the note
                atomicNotesDomain.saveAtomicNote(note);
                return notebook;
            }, updatedNotebook -> {
                // No need to update UI if nothing changed in the notebook structure
            });
        }
    }
    
    public void deleteNote(AtomicNoteEntity note) {
        SmartNotebook notebook = smartNotebook.getValue();
        
        if (notebook != null) {
            BackgroundOps.execute(() -> {
                notebook.removeNote(note.getNoteId());
                
                if (notebook.atomicNotes.isEmpty()) {
                    smartNotebookRepository.deleteSmartNotebook(notebook);
                    return null;
                } else {
                    return notebook;
                }
            }, updatedNotebook -> {
                if (updatedNotebook == null) {
                    EventBus.getDefault().post(new Events.NotebookDeleted());
                } else {
                    smartNotebook.setValue(updatedNotebook);
                    // Adjust current page index if needed
                    int newIndex = Math.min(currentPageIndex.getValue(), 
                                         updatedNotebook.getAtomicNotes().size() - 1);
                    setCurrentPageIndex(newIndex);
                }
            });
        }
    }
    
    public void setCurrentPageIndex(int index) {
        SmartNotebook notebook = smartNotebook.getValue();
        if (notebook != null) {
            currentPageIndex.setValue(index);
            updatePageCounters(notebook, index);
        }
    }
    
    private void updatePageCounters(SmartNotebook notebook, int currentIndex) {
        int totalPagesCount = notebook.getAtomicNotes().size();
        totalPages.setValue(totalPagesCount);
        
        boolean showNext = totalPagesCount > 1 && currentIndex < totalPagesCount - 1;
        boolean showPrev = totalPagesCount > 1 && currentIndex > 0;
        
        showNextButton.setValue(showNext);
        showPrevButton.setValue(showPrev);
    }
    
    // LiveData getters
    public LiveData<SmartNotebook> getSmartNotebook() {
        return smartNotebook;
    }
    
    public LiveData<Integer> getCurrentPageIndex() {
        return currentPageIndex;
    }
    
    public LiveData<Integer> getTotalPages() {
        return totalPages;
    }
    
    public LiveData<Boolean> getShowNextButton() {
        return showNextButton;
    }
    
    public LiveData<Boolean> getShowPrevButton() {
        return showPrevButton;
    }
} 