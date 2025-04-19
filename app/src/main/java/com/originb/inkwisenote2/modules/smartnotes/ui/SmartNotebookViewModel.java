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
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
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

//    public class SmartNotebookUpdate {
//        public SmartNotebook smartNotebook;
//        public AtomicNoteEntity updatedAtomicNote;
//    }

    public SmartNotebookViewModel(@NonNull Application application) {
        super(application);
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        atomicNotesDomain = Repositories.getInstance().getAtomicNotesDomain();
        handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
        textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();

        EventBus.getDefault().register(this);
    }

    public LiveData<Integer> getCurrentPageIndexLive() {
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

    public void saveCurrentNote(NoteHolderData noteHolderData) {
        SmartNotebook notebook = smartNotebook.getValue();

        switch (noteHolderData.noteType) {
            case HANDWRITTEN_PNG:
                handwrittenNoteRepository.saveHandwrittenNotes(notebook.smartBook.getBookId(),
                        getCurrentNote(),
                        noteHolderData.bitmap,
                        noteHolderData.pageTemplate);
                break;
            case TEXT_NOTE:
                AtomicNoteEntity atomicNote = getCurrentNote();
                TextNoteEntity textNoteEntity = textNotesDao.getTextNoteForNote(atomicNote.getNoteId());
                if (textNoteEntity == null) {
                    textNoteEntity = new TextNoteEntity(
                            atomicNote.getNoteId(),
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
        int currentNoteIndex = currentPageIndexLive.getValue();
        SmartNotebook notebook = smartNotebook.getValue();
        return notebook.getAtomicNotes().get(currentNoteIndex);
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