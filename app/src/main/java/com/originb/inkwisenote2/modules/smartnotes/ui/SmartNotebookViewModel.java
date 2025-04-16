package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPage;

import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SmartNotebookViewModel extends AndroidViewModel {
    private final Logger logger = new Logger("SmartNotebookViewModel");

    private final SmartNotebookRepository smartNotebookRepository;
    private final AtomicNotesDomain atomicNotesDomain;
    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private final TextNotesDao textNotesDao;

    private String workingNotePath;
    private final MutableLiveData<Integer> currentPageIndexLive = new MutableLiveData<>(0);
    private final MutableLiveData<SmartNotebook> smartNotebook = new MutableLiveData<>();
    private final MutableLiveData<String> notebookTitle = new MutableLiveData<>("");
    private final MutableLiveData<String> pageNumberText = new MutableLiveData<>("");
    private final MutableLiveData<Long> createdTimeMillis = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showNextButton = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> showPrevButton = new MutableLiveData<>(false);
    
    // New LiveData to notify when a note type changes
    private final MutableLiveData<Integer> noteTypeChangedPosition = new MutableLiveData<>();

    public SmartNotebookViewModel(@NonNull Application application) {
        super(application);
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        atomicNotesDomain = Repositories.getInstance().getAtomicNotesDomain();
        handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
        textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();

        EventBus.getDefault().register(this);
    }

    public MutableLiveData<Integer> getCurrentPageIndexLive() {
        return currentPageIndexLive;
    }

    public LiveData<SmartNotebook> getSmartNotebook() {
        return smartNotebook;
    }

    public LiveData<String> getNotebookTitle() {
        return notebookTitle;
    }

    public LiveData<String> getPageNumberText() {
        return pageNumberText;
    }

    public LiveData<Long> getCreatedTimeMillis() {
        return createdTimeMillis;
    }

    public LiveData<Boolean> getShowNextButton() {
        return showNextButton;
    }

    public LiveData<Boolean> getShowPrevButton() {
        return showPrevButton;
    }
    
    public LiveData<Integer> getNoteTypeChangedPosition() {
        return noteTypeChangedPosition;
    }

    public void loadSmartNotebook(Long bookId, String workingPath, String noteIdsString) {
        this.workingNotePath = workingPath;
        BackgroundOps.executeOpt(
                () -> getSmartNotebook(bookId, workingPath, noteIdsString),
                notebook -> {
                    smartNotebook.setValue(notebook);
                    notebookTitle.setValue(notebook.smartBook.getTitle());
                    createdTimeMillis.setValue(notebook.smartBook.getCreatedTimeMillis());
                    updatePageNumberText();
                    updateNavigationButtons();
                }
        );
    }

    public void navigateToNextPage() {
        SmartNotebook notebook = smartNotebook.getValue();
        if (notebook == null) return;

        int currentIndex = currentPageIndexLive.getValue();
        int nextIndex = currentIndex + 1;
        if (nextIndex < notebook.getAtomicNotes().size()) {
            currentPageIndexLive.setValue(nextIndex);
            updatePageNumberText();
            updateNavigationButtons();
        }
    }

    public void navigateToPreviousPage() {
        SmartNotebook notebook = smartNotebook.getValue();
        if (notebook == null) return;

        int currentIndex = currentPageIndexLive.getValue();
        int prevIndex = currentIndex - 1;

        if (prevIndex >= 0) {
            currentPageIndexLive.setValue(prevIndex);
            updatePageNumberText();
            updateNavigationButtons();
        }
    }

    public void addNewPage() {
        SmartNotebook notebook = smartNotebook.getValue();
        if (notebook == null) return;

        int currentIndex = currentPageIndexLive.getValue();
        int indexToInsertAt = currentIndex + 1;

        BackgroundOps.execute(() -> {
            AtomicNoteEntity newAtomicNote = atomicNotesDomain.saveAtomicNote(
                    AtomicNotesDomain.constructAtomicNote("", workingNotePath, NoteType.NOT_SET)
            );

            SmartBookPage newSmartPage = smartNotebookRepository.newSmartBookPage(
                    notebook.smartBook, newAtomicNote, indexToInsertAt
            );

            notebook.insertAtomicNoteAndPage(indexToInsertAt, newAtomicNote, newSmartPage);
            return notebook;
        }, updatedNotebook -> {
            smartNotebook.setValue(updatedNotebook);
            currentPageIndexLive.setValue(indexToInsertAt);
            updatePageNumberText();
            updateNavigationButtons();
        });
    }
    
    /**
     * Get an atomic note by its ID
     * @param noteId The note ID to find
     * @return The atomic note or null if not found
     */
    public AtomicNoteEntity getNoteById(long noteId) {
        SmartNotebook notebook = smartNotebook.getValue();
        if (notebook == null) return null;
        
        return notebook.getAtomicNotes().stream()
                .filter(note -> note.getNoteId() == noteId)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Update the type of a note and notify listeners
     * @param note The note to update
     * @param newType The new note type
     */
    public void updateNoteType(AtomicNoteEntity note, NoteType newType) {
        SmartNotebook notebook = smartNotebook.getValue();
        if (notebook == null) return;
        
        note.setNoteType(newType.toString());
        
        int position = notebook.getAtomicNotes().indexOf(note);
        if (position != -1) {
            BackgroundOps.execute(() -> {
                smartNotebookRepository.updateNotebook(notebook, getApplication());
                return position;
            }, updatedPosition -> {
                // Notify that the note type changed at this position
                noteTypeChangedPosition.setValue(updatedPosition);
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteDelete(Events.NoteDeleted noteDeleted) {
        SmartNotebook notebook = smartNotebook.getValue();
        if (notebook == null) return;

        long noteId = noteDeleted.atomicNote.getNoteId();
        notebook.removeNote(noteId);

        if (notebook.atomicNotes.isEmpty()) {
            BackgroundOps.execute(() -> smartNotebookRepository.deleteSmartNotebook(notebook));
            return;
        }

        smartNotebook.setValue(notebook);
        updatePageNumberText();
        updateNavigationButtons();
    }

    public void updateTitle(String title) {
        SmartNotebook notebook = smartNotebook.getValue();
        if (notebook == null) return;

        notebook.getSmartBook().setTitle(title);
        notebookTitle.setValue(title);
        BackgroundOps.execute(() -> smartNotebookRepository.updateNotebook(notebook, getApplication()));
    }

    public void saveCurrentSmartNotebook() {
        // This method is called when the activity is paused or stopped
        SmartNotebook notebook = smartNotebook.getValue();
        String title = notebookTitle.getValue();
        if (notebook == null || title == null) return;

        notebook.getSmartBook().setTitle(title);
        BackgroundOps.execute(() -> smartNotebookRepository.updateNotebook(notebook, getApplication()));
    }

    public void saveCurrentNote(NoteHolder.NoteHolderData noteHolderData) {
        SmartNotebook notebook = smartNotebook.getValue();
        if (notebook == null) return;

        AtomicNoteEntity currentNote = getCurrentNote();
        if (currentNote == null) return;

        switch (noteHolderData.noteType) {
            case HANDWRITTEN_PNG:
                handwrittenNoteRepository.saveHandwrittenNotes(notebook.smartBook.getBookId(),
                        currentNote,
                        noteHolderData.bitmap,
                        noteHolderData.pageTemplate);
                break;
            case TEXT_NOTE:
                TextNoteEntity textNoteEntity = textNotesDao.getTextNoteForNote(currentNote.getNoteId());
                if (textNoteEntity == null) {
                    textNoteEntity = new TextNoteEntity(
                            currentNote.getNoteId(),
                            notebook.smartBook.getBookId());
                    textNotesDao.insertTextNote(textNoteEntity);
                } else {
                    textNoteEntity.setNoteText(noteHolderData.noteText);
                    textNotesDao.updateTextNote(textNoteEntity);
                }
                break;
            default:
        }
    }

    public AtomicNoteEntity getCurrentNote() {
        SmartNotebook notebook = smartNotebook.getValue();
        Integer currentIndex = currentPageIndexLive.getValue();
        
        if (notebook == null || currentIndex == null || 
            currentIndex < 0 || currentIndex >= notebook.getAtomicNotes().size()) {
            return null;
        }
        
        return notebook.getAtomicNotes().get(currentIndex);
    }

    private Optional<SmartNotebook> getSmartNotebook(Long bookIdToOpen, String workingPath, String noteIdsString) {
        workingNotePath = workingPath;

        if (bookIdToOpen != null && bookIdToOpen != -1) {
            return smartNotebookRepository.getSmartNotebooks(bookIdToOpen);
        } else if (noteIdsString != null && !noteIdsString.isEmpty()) {
            String[] noteIds = noteIdsString.split(",");
            Set<Long> noteIdsSet = Arrays.stream(noteIds)
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
            return smartNotebookRepository.getVirtualSmartNotebooks(noteIdsSet);
        }

        return smartNotebookRepository.initializeNewSmartNotebook(
                "", workingNotePath, NoteType.NOT_SET);
    }

    private void updatePageNumberText() {
        SmartNotebook notebook = smartNotebook.getValue();
        Integer index = currentPageIndexLive.getValue();
        if (notebook == null || index == null) return;

        String text = (index + 1) + "/" + notebook.getAtomicNotes().size();
        pageNumberText.setValue(text);
    }

    private void updateNavigationButtons() {
        SmartNotebook notebook = smartNotebook.getValue();
        Integer index = currentPageIndexLive.getValue();
        if (notebook == null || index == null) return;

        int totalPages = notebook.getAtomicNotes().size();
        showNextButton.setValue(index < totalPages - 1);
        showPrevButton.setValue(index > 0);
    }

    @Override
    protected void onCleared() {
        EventBus.getDefault().unregister(this);
        super.onCleared();
    }
}