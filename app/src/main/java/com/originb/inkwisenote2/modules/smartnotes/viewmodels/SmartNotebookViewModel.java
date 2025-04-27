package com.originb.inkwisenote2.modules.smartnotes.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.*;

import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SmartNotebookViewModel extends AndroidViewModel {
    private final Logger logger = new Logger("SmartNotebookViewModel");

    private final SmartNotebookRepository smartNotebookRepository;
    private final AtomicNotesDomain atomicNotesDomain;
    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private final TextNotesDao textNotesDao;

    private String workingNotePath;
    private final MutableLiveData<Integer> currentPageIndexLive = new MutableLiveData<>(0);
    private final MutableLiveData<SmartNotebookUpdate> smartNotebook = new MutableLiveData<>();
    private final MutableLiveData<String> notebookTitle = new MutableLiveData<>("");
    private final MutableLiveData<NotebookNavigationData> navigationDataLive = new MutableLiveData<>(new NotebookNavigationData());
    private final MutableLiveData<Long> createdTimeMillis = new MutableLiveData<>();

    public void onNotebookIsInDb(Consumer<Boolean> ifNotebookIsSaved) {
        SmartNotebookViewModel.SmartNotebookUpdate smartNotebookUpdate = getSmartNotebook().getValue();
        if (smartNotebookUpdate == null) return;
        SmartNotebook notebook = smartNotebookUpdate.smartNotebook;
        if (notebook == null) return;

        BackgroundOps.execute(() -> smartNotebookRepository.bookExists(notebook),
                ifNotebookIsSaved);
    }

    public static class SmartNotebookUpdate {
        public SmartNotebookUpdateType notbookUpdateType = SmartNotebookUpdateType.NOTE_UPDATE;
        public SmartNotebook smartNotebook;
        public AtomicNoteEntity atomicNote;
        public int indexOfUpdatedNote = -1;

        public static SmartNotebookUpdate fromNotebook(SmartNotebook smartNotebook) {
            SmartNotebookUpdate smartNotebookUpdate = new SmartNotebookUpdate();
            smartNotebookUpdate.smartNotebook = smartNotebook;
            return smartNotebookUpdate;
        }

        public static SmartNotebookUpdate noteDeleted(SmartNotebook smartNotebook, AtomicNoteEntity atomicNote) {
            SmartNotebookUpdate smartNotebookUpdate = new SmartNotebookUpdate();
            smartNotebookUpdate.atomicNote = atomicNote;
            smartNotebookUpdate.smartNotebook = smartNotebook;
            smartNotebookUpdate.notbookUpdateType = SmartNotebookUpdateType.NOTE_DELETED;
            return smartNotebookUpdate;
        }

        public static SmartNotebookUpdate fromNoteAndBook(SmartNotebook updatedNotebook,
                                                          int indexOfUpdatedNote) {
            SmartNotebookUpdate smartNotebookUpdate = new SmartNotebookUpdate();
            smartNotebookUpdate.smartNotebook = updatedNotebook;
            smartNotebookUpdate.indexOfUpdatedNote = indexOfUpdatedNote;
            return smartNotebookUpdate;
        }

        public static SmartNotebookUpdate notebookDeleted(SmartNotebook deletedNotebook) {
            SmartNotebookUpdate smartNotebookUpdate = new SmartNotebookUpdate();
            smartNotebookUpdate.smartNotebook = deletedNotebook;
            smartNotebookUpdate.notbookUpdateType = SmartNotebookUpdateType.NOTEBOOK_DELETED;
            return smartNotebookUpdate;
        }
    }

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

    public LiveData<SmartNotebookUpdate> getSmartNotebook() {
        return smartNotebook;
    }

    public LiveData<String> getNotebookTitle() {
        return notebookTitle;
    }

    public LiveData<NotebookNavigationData> getNavigationDataLive() {
        return navigationDataLive;
    }

    public LiveData<Long> getCreatedTimeMillis() {
        return createdTimeMillis;
    }

    public void loadSmartNotebook(Long bookId, String workingPath, String bookTitle, String noteIdsString) {
        this.workingNotePath = workingPath;
        BackgroundOps.executeOpt(
                () -> getSmartNotebook(bookId, workingPath, bookTitle, noteIdsString),
                notebook -> {
                    smartNotebook.setValue(SmartNotebookUpdate.fromNotebook(notebook));
                    notebookTitle.setValue(notebook.smartBook.getTitle());
                    createdTimeMillis.setValue(notebook.smartBook.getCreatedTimeMillis());
                    updatePageNumberText();
                }
        );
    }

    public void navigateToNextPage() {
        SmartNotebook notebook = smartNotebook.getValue().smartNotebook;
        if (notebook == null) return;

        int currentIndex = currentPageIndexLive.getValue();
        int nextIndex = currentIndex + 1;
        if (nextIndex < notebook.getAtomicNotes().size()) {
            currentPageIndexLive.setValue(nextIndex);
            updatePageNumberText();
        }
    }

    public void navigateToPreviousPage() {
        SmartNotebook notebook = smartNotebook.getValue().smartNotebook;
        if (notebook == null) return;

        int currentIndex = currentPageIndexLive.getValue();
        int prevIndex = currentIndex - 1;

        if (prevIndex >= 0) {
            currentPageIndexLive.setValue(prevIndex);
            updatePageNumberText();
        }
    }

    public void addNewPage() {
        SmartNotebook notebook = smartNotebook.getValue().smartNotebook;
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
            smartNotebook.setValue(SmartNotebookUpdate.fromNoteAndBook(updatedNotebook, indexToInsertAt));
            currentPageIndexLive.setValue(indexToInsertAt);
            updatePageNumberText();
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotebookDelete(Events.NotebookDeleted notebookDeleted) {
        SmartNotebook notebook = smartNotebook.getValue().smartNotebook;
        SmartNotebook deletedNotebook = notebookDeleted.smartNotebook;
        if (notebook == null || deletedNotebook.smartBook.getBookId() != notebook.smartBook.getBookId()) return;

        smartNotebook.setValue(SmartNotebookUpdate.notebookDeleted(deletedNotebook));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteDelete(Events.NoteDeleted noteDeleted) {
        SmartNotebook notebook = smartNotebook.getValue().smartNotebook;
        if (notebook == null) return;

        long noteId = noteDeleted.atomicNote.getNoteId();
        notebook.removeNote(noteId);

        if (notebook.atomicNotes.isEmpty()) {
            BackgroundOps.execute(() -> smartNotebookRepository.deleteSmartNotebook(notebook));
            return;
        }

        smartNotebook.setValue(SmartNotebookUpdate.noteDeleted(notebook, noteDeleted.atomicNote));

        int currentPageIndex = currentPageIndexLive.getValue();
        if (currentPageIndex == notebook.getAtomicNotes().size()) {
            currentPageIndexLive.setValue(currentPageIndex - 1);
        }

        updatePageNumberText();
    }

    public void updateTitle(String title) {
        SmartNotebook notebook = smartNotebook.getValue().smartNotebook;
        if (notebook == null) return;

        notebook.getSmartBook().setTitle(title);
        notebookTitle.setValue(title);
        BackgroundOps.execute(() -> smartNotebookRepository.updateNotebook(notebook, getApplication()));
    }

    public void saveCurrentSmartNotebook() {
        // This method is called when the activity is paused or stopped
        SmartNotebook notebook = smartNotebook.getValue().smartNotebook;
        String title = notebookTitle.getValue();
        if (notebook == null || title == null) return;

        notebook.getSmartBook().setTitle(title);
        if (notebook.smartBook.getBookId() > -1) {
            BackgroundOps.execute(() -> smartNotebookRepository.updateNotebook(notebook, getApplication()));
        } else {
            BackgroundOps.execute(() -> smartNotebookRepository.saveSmartNotebook(notebook, getApplication()));
        }
    }

    public void saveCurrentNote(NoteHolderData noteHolderData) {
        SmartNotebook notebook = smartNotebook.getValue().smartNotebook;

        switch (noteHolderData.noteType) {
            case HANDWRITTEN_PNG:
                handwrittenNoteRepository.saveHandwrittenNotes(notebook.smartBook.getBookId(),
                        getCurrentNote(),
                        noteHolderData.bitmap,
                        noteHolderData.pageTemplate,
                        noteHolderData.strokes,
                        getApplication());
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
                // TODO: should be in text note repository
                EventBus.getDefault().post(new Events.TextNoteSaved(notebook.smartBook.getBookId(),
                        atomicNote,
                        getApplication()));
                break;
            default:
        }
    }

    public AtomicNoteEntity getCurrentNote() {
        int currentNoteIndex = currentPageIndexLive.getValue();
        SmartNotebook notebook = smartNotebook.getValue().smartNotebook;
        return notebook.getAtomicNotes().get(currentNoteIndex);
    }

    private Optional<SmartNotebook> getSmartNotebook(Long bookIdToOpen,
                                                     String workingPath,
                                                     String bookTitle,
                                                     String noteIdsString) {
        workingNotePath = workingPath;

        if (bookIdToOpen != null && bookIdToOpen != -1) {
            return smartNotebookRepository.getSmartNotebooks(bookIdToOpen);
        } else if (noteIdsString != null && !noteIdsString.isEmpty()) {
            String[] noteIds = noteIdsString.split(",");
            Set<Long> noteIdsSet = Arrays.stream(noteIds)
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());

            Set<SmartNotebook> smartNotebooks = smartNotebookRepository.getSmartNotebooksForNoteIds(noteIdsSet);
            if (smartNotebooks.size() == 1) {
                return smartNotebooks.stream().findFirst();
            }

            return smartNotebookRepository.getVirtualSmartNotebooks(bookTitle, noteIdsSet);
        }

        return smartNotebookRepository.initializeNewSmartNotebook(
                "", workingNotePath, NoteType.NOT_SET);
    }

    private void updatePageNumberText() {
        SmartNotebook notebook = smartNotebook.getValue().smartNotebook;
        Integer index = currentPageIndexLive.getValue();
        if (notebook == null || index == null) return;

        String text = (index + 1) + "/" + notebook.getAtomicNotes().size();

        NotebookNavigationData navigationData = navigationDataLive.getValue();
        navigationData.pageNumbeText = text;

        int totalPages = notebook.getAtomicNotes().size();
        navigationData.showNextButton = (index < totalPages - 1);
        navigationData.showPrevButton = (index > 0);
        navigationDataLive.setValue(navigationData);
    }

    @Override
    protected void onCleared() {
        EventBus.getDefault().unregister(this);
        super.onCleared();
    }
}