package com.originb.inkwisenote2.modules.smartnotes.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.common.Strings;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SmartNotebookViewModel extends AndroidViewModel {
    private final Logger logger = new Logger("SmartNotebookViewModel");

    private final SmartNotebookRepository smartNotebookRepository;
    private final AtomicNotesDomain atomicNotesDomain;
    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private final TextNotesDao textNotesDao;

    // Lock for synchronizing page navigation
    private final Object pageSwitchLock = new Object();

    private String workingNotePath;
    private final MutableLiveData<Integer> currentPageIndexLive = new MutableLiveData<>(0);
    private final MutableLiveData<SmartNotebookUpdate> smartNotebookUpdate = new MutableLiveData<>();
    private final MutableLiveData<String> notebookTitle = new MutableLiveData<>("");
    private final MutableLiveData<NotebookNavigationData> navigationDataLive = new MutableLiveData<>(new NotebookNavigationData());
    private final MutableLiveData<Long> createdTimeMillis = new MutableLiveData<>();

    public void onNotebookIsInDb(Consumer<Boolean> ifNotebookIsSaved) {
        SmartNotebookViewModel.SmartNotebookUpdate smartNotebookUpdate = getSmartNotebookUpdate().getValue();
        if (smartNotebookUpdate == null) return;
        SmartNotebook notebook = smartNotebookUpdate.smartNotebook;
        if (notebook == null) return;

        BackgroundOps.execute(() -> smartNotebookRepository.bookExists(notebook),
                ifNotebookIsSaved);
    }

    public void setNotebookTitle(String notebookTitle) {
        SmartNotebookUpdate notebookUpdate = smartNotebookUpdate.getValue();
        notebookUpdate.smartNotebook.smartBook.setTitle(notebookTitle);
        notebookUpdate.notbookUpdateType = SmartNotebookUpdateType.NOTEBOOK_TITLE_UPDATED;

        smartNotebookUpdate.setValue(notebookUpdate);
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

    public LiveData<SmartNotebookUpdate> getSmartNotebookUpdate() {
        return smartNotebookUpdate;
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
        if (!Strings.isNullOrWhitespace(bookTitle)) {
            this.workingNotePath = Paths.get(workingPath, bookTitle).toString();
        }

        BackgroundOps.executeOpt(
                () -> getSmartNotebook(bookId, bookTitle, noteIdsString),
                notebook -> {
                    smartNotebookUpdate.setValue(SmartNotebookUpdate.fromNotebook(notebook));
                    notebookTitle.setValue(notebook.smartBook.getTitle());
                    createdTimeMillis.setValue(notebook.smartBook.getCreatedTimeMillis());
                    updatePageNumberText();


                    if (bookTitle == null) {
                        String booktTitleFromDb = notebook.smartBook.getTitle();
                        booktTitleFromDb = booktTitleFromDb != null ?
                                booktTitleFromDb : String.valueOf(notebook.smartBook.getCreatedTimeMillis());
                        this.workingNotePath = Paths.get(workingPath, booktTitleFromDb).toString();
                    }
                }
        );
    }

    public void navigateToPageIndex(int pageIndex) {
        SmartNotebook notebook = smartNotebookUpdate.getValue().smartNotebook;
        if (notebook == null) return;
        if (pageIndex < notebook.getAtomicNotes().size()) {
            currentPageIndexLive.setValue(pageIndex);
            updatePageNumberText();
        }
    }

    public void navigateToNextPage() {
        SmartNotebook notebook = smartNotebookUpdate.getValue().smartNotebook;
        if (notebook == null) return;

        int currentIndex = currentPageIndexLive.getValue();
        int nextIndex = currentIndex + 1;
        if (nextIndex < notebook.getAtomicNotes().size()) {
            currentPageIndexLive.setValue(nextIndex);
            updatePageNumberText();
        }
    }

    public void navigateToPreviousPage() {
        SmartNotebook notebook = smartNotebookUpdate.getValue().smartNotebook;
        if (notebook == null) return;

        int currentIndex = currentPageIndexLive.getValue();
        int prevIndex = currentIndex - 1;

        if (prevIndex >= 0) {
            currentPageIndexLive.setValue(prevIndex);
            updatePageNumberText();
        }
    }

    public void addNewPage() {
        SmartNotebook notebook = smartNotebookUpdate.getValue().smartNotebook;
        if (notebook == null) return;

        int currentIndex = currentPageIndexLive.getValue();
        int indexToInsertAt = currentIndex + 1;

        BackgroundOps.execute(() -> {
            AtomicNoteEntity newAtomicNote = atomicNotesDomain.saveAtomicNote(
                    AtomicNotesDomain.constructAtomicNote("",
                            workingNotePath,
                            NoteType.NOT_SET)
            );

            SmartBookPage newSmartPage = smartNotebookRepository.newSmartBookPage(
                    notebook.smartBook, newAtomicNote, indexToInsertAt
            );

            notebook.insertAtomicNoteAndPage(indexToInsertAt, newAtomicNote, newSmartPage);
            return notebook;
        }, updatedNotebook -> {
            smartNotebookUpdate.setValue(SmartNotebookUpdate.fromNoteAndBook(updatedNotebook, indexToInsertAt));
            currentPageIndexLive.setValue(indexToInsertAt);
            updatePageNumberText();
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotebookDelete(Events.NotebookDeleted notebookDeleted) {
        SmartNotebook notebook = smartNotebookUpdate.getValue().smartNotebook;
        SmartNotebook deletedNotebook = notebookDeleted.smartNotebook;
        if (notebook == null || deletedNotebook.smartBook.getBookId() != notebook.smartBook.getBookId())
            return;
        deleteNotebookFolder(notebook.smartBook.getTitle());
        smartNotebookUpdate.setValue(SmartNotebookUpdate.notebookDeleted(deletedNotebook));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteDelete(Events.NoteDeleted noteDeleted) {
        SmartNotebook notebook = smartNotebookUpdate.getValue().smartNotebook;
        if (notebook == null) return;

        long noteId = noteDeleted.atomicNote.getNoteId();
        notebook.removeNote(noteId);

        if (notebook.atomicNotes.isEmpty()) {
            BackgroundOps.execute(() -> smartNotebookRepository.deleteSmartNotebook(notebook),
                    () -> deleteNotebookFolder(notebook.smartBook.getTitle()));
            return;
        }

        smartNotebookUpdate.setValue(SmartNotebookUpdate.noteDeleted(notebook, noteDeleted.atomicNote));

        int currentPageIndex = currentPageIndexLive.getValue();
        if (currentPageIndex == notebook.getAtomicNotes().size()) {
            currentPageIndexLive.setValue(currentPageIndex - 1);
        }

        updatePageNumberText();
    }

    private void deleteNotebookFolder(String smartNotebookTitle) {
        if (smartNotebookTitle == null) {
            smartNotebookTitle = "";
        }
        Path notebookPath = Paths.get(workingNotePath, smartNotebookTitle);
        try {
            Files.walk(notebookPath)
                    .sorted(Comparator.reverseOrder()) // Delete children before parent
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Handle exception (file might be locked, etc)
                            System.err.println("Unable to delete: " + path + " : " + e.getMessage());
                        }
                    });
            Files.delete(notebookPath);
        } catch (Exception e) {
            System.out.println("Failed to delete the file: " + e.getMessage());
        }
    }

    public void saveNoteInCorrectFolder(AtomicNoteEntity atomicNote,
                                        String newNotebookPath,
                                        NoteHolderData noteHolderData) {
        // Store the old path to move files
        String oldFilePath = atomicNote.getFilepath();

        // Update the filepath
        atomicNote.setFilepath(newNotebookPath);

        // Ensure directory exists
        File directory = new File(newNotebookPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Move existing files if they exist
        moveFileIfExists(oldFilePath, newNotebookPath, atomicNote.getFilename() + ".png");
        moveFileIfExists(oldFilePath, newNotebookPath, atomicNote.getFilename() + "-t.png");
        moveFileIfExists(oldFilePath, newNotebookPath, atomicNote.getFilename() + ".pt");
        moveFileIfExists(oldFilePath, newNotebookPath, atomicNote.getFilename() + ".md");

        BackgroundOps.execute(() -> {
            atomicNotesDomain.updateAtomicNote(atomicNote);
            saveCurrentNote(atomicNote, noteHolderData);
        });
    }

    /**
     * Moves a file from source to destination if it exists
     */
    private void moveFileIfExists(String sourcePath, String destPath, String filename) {
        try {
            File sourceFile = new File(sourcePath, filename);
            if (sourceFile.exists()) {
                File destFile = new File(destPath, filename);
                if (!sourceFile.renameTo(destFile)) {
                    logger.error("Failed to move file: " + filename);
                }
            }
        } catch (Exception e) {
            logger.exception("Error moving file: " + filename, e);
        }
    }

    public boolean updateTitle(String updatedTitle) {
        if (smartNotebookUpdate.getValue() == null) return false;
        SmartNotebook notebook = smartNotebookUpdate.getValue().smartNotebook;
        if (notebook == null) return false;

        if (Strings.isNotEmpty(updatedTitle)) {
            notebook.getSmartBook().setTitle(updatedTitle);
            notebookTitle.setValue(updatedTitle);
            return true;
        }
        return false;
    }

    public boolean renameNotebookFolderName(String newNotebookPath, String oldNotebookTitle) {
        File newFolder = new File(newNotebookPath);

        boolean updateNotesFolderName = true;

        File oldFolder = new File(workingNotePath + "/" + oldNotebookTitle);
        updateNotesFolderName = oldFolder.renameTo(newFolder);
        if (!newFolder.exists()) {
            updateNotesFolderName = newFolder.mkdirs();
        }
        return updateNotesFolderName;
    }


    public void saveCurrentSmartNotebook() {
        // This method is called when the activity is paused or stopped
        SmartNotebook notebook = smartNotebookUpdate.getValue().smartNotebook;
        String title = notebookTitle.getValue();
        if (notebook == null || title == null) return;

        notebook.getSmartBook().setTitle(title);
        if (notebook.smartBook.getBookId() > -1) {
            BackgroundOps.execute(() -> smartNotebookRepository.updateNotebook(notebook, getApplication()));
        } else {
            BackgroundOps.execute(() -> smartNotebookRepository.saveSmartNotebook(notebook, getApplication()));
        }
    }

    public void saveCurrentNote(AtomicNoteEntity atomicNote, NoteHolderData noteData) {
        if (atomicNote == null || noteData == null) {
            logger.error("Attempted to save null note or note data");
            return;
        }

        SmartNotebook notebook = smartNotebookUpdate.getValue().smartNotebook;
        if (notebook == null) return;

        synchronized (pageSwitchLock) {
            try {
                saveCurrentNoteAsPerType(atomicNote, noteData, notebook);
            } catch (Exception e) {
                logger.exception("Error saving note", e);
            }
        }
    }

    public void saveCurrentNoteAsPerType(AtomicNoteEntity atomicNote, NoteHolderData noteData, SmartNotebook notebook) {

        switch (noteData.noteType) {
            case HANDWRITTEN_PNG:
                handwrittenNoteRepository.saveHandwrittenNotes(notebook.smartBook.getBookId(),
                        atomicNote,
                        noteData.bitmap,
                        noteData.pageTemplate,
                        noteData.strokes,
                        getApplication());
                break;
            case TEXT_NOTE:
                // Save to database
                TextNoteEntity textNoteEntity = textNotesDao.getTextNoteForNote(atomicNote.getNoteId());
                if (textNoteEntity == null) {
                    textNoteEntity = new TextNoteEntity(
                            atomicNote.getNoteId(),
                            notebook.smartBook.getBookId());
                    textNotesDao.insertTextNote(textNoteEntity);
                }
                textNoteEntity.setNoteText(noteData.noteText);
                textNotesDao.updateTextNote(textNoteEntity);

                // Save to markdown file
                saveNoteToMarkdownFile(atomicNote, noteData.noteText);

                // Post event
                EventBus.getDefault().post(new Events.TextNoteSaved(notebook.smartBook.getBookId(),
                        atomicNote,
                        getApplication()));
                break;
            default:
                // Do nothing
        }
    }

    /**
     * Save note text to a markdown file
     */
    private void saveNoteToMarkdownFile(AtomicNoteEntity note, String noteText) {
        BackgroundOps.execute(() -> {
            String filename = note.getFilename() + ".md";
            // Ensure we're using the notebook's directory path
            String notebookDir = workingNotePath;
            if (Strings.isNullOrWhitespace(notebookDir)) {
                notebookDir = note.getFilepath();
            }
            File file = new File(notebookDir, filename);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(noteText);
                return true;
            } catch (IOException e) {
                logger.exception("Error saving markdown file", e);
                return false;
            }
        }, success -> {
            if (!success) {
                logger.error("Failed to save markdown file for note: " + note.getNoteId());
            }
        });
    }

    public AtomicNoteEntity getCurrentNote() {
        // todo: handle this null case
        if (smartNotebookUpdate.getValue() == null) {
            return null;
        }

        int currentNoteIndex = currentPageIndexLive.getValue();
        SmartNotebook notebook = smartNotebookUpdate.getValue().smartNotebook;
        return notebook.getAtomicNotes().get(currentNoteIndex);
    }

    private Optional<SmartNotebook> getSmartNotebook(Long bookIdToOpen,
                                                     String bookTitle,
                                                     String noteIdsString) {
        if (bookIdToOpen != null && bookIdToOpen != -1) {
            return smartNotebookRepository.getSmartNotebooks(bookIdToOpen);
        } else if (noteIdsString != null && !noteIdsString.isEmpty()) {
            String[] noteIds = noteIdsString.split(",");
            Set<Long> noteIdsSet = Arrays.stream(noteIds)
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());

            Set<SmartNotebook> smartNotebooks = smartNotebookRepository.getSmartNotebooksForNoteIds(noteIdsSet);
            if (smartNotebooks.size() == 1) { // if all the notes belong to an existing notebook
                Optional<SmartNotebook> notebookWithAllNotes = smartNotebooks.stream().findFirst();
                Set<Long> noteIdsInNotebook = notebookWithAllNotes.get().atomicNotes
                        .stream().map(AtomicNoteEntity::getNoteId)
                        .collect(Collectors.toSet());

                noteIdsInNotebook.removeAll(noteIdsSet);
                if (!noteIdsInNotebook.isEmpty()) return notebookWithAllNotes;
            }

            return smartNotebookRepository.getVirtualSmartNotebooks(bookTitle, noteIdsSet);
        }

        return smartNotebookRepository.initializeNewSmartNotebook(
                "", workingNotePath, NoteType.NOT_SET);
    }

    private void updatePageNumberText() {
        SmartNotebook notebook = smartNotebookUpdate.getValue().smartNotebook;
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