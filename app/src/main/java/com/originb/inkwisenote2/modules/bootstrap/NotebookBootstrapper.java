package com.originb.inkwisenote2.modules.bootstrap;

import android.content.Context;
import android.util.Log;

import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.common.Strings;
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPage;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPagesDao;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Class for bootstrapping the app by scanning the working directory for notebooks and notes
 */
public class NotebookBootstrapper {
    private static final String TAG = "NotebookBootstrapper";
    private final Logger logger = new Logger(TAG);

    private final SmartNotebookRepository smartNotebookRepository;
    private final AtomicNotesDomain atomicNotesDomain;
    private final SmartBookPagesDao smartBookPagesDao;

    public NotebookBootstrapper() {
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        this.atomicNotesDomain = Repositories.getInstance().getAtomicNotesDomain();
        this.smartBookPagesDao = Repositories.getInstance().getNotesDb().smartBookPagesDao();
    }

    /**
     * Scan the working directory for notebooks and notes
     *
     * @param context          Android context
     * @param workingDirectory The directory to scan
     * @return List of bootstrapped SmartNotebooks
     */
    public List<SmartNotebook> bootstrapFromDirectory(Context context, File workingDirectory) {
        List<SmartNotebook> bootstrappedNotebooks = new ArrayList<>();

        if (!workingDirectory.exists() || !workingDirectory.isDirectory()) {
            logger.error("Invalid working directory: " + workingDirectory.getAbsolutePath());
            return bootstrappedNotebooks;
        }

        logger.debug("Bootstrapping from directory: " + workingDirectory.getAbsolutePath());

        // Get all files and folders in the working directory
        File[] files = workingDirectory.listFiles();
        if (files == null || files.length == 0) {
            logger.debug("No files found in working directory");
            return bootstrappedNotebooks;
        }

        // Process directories as notebooks
        Arrays.stream(files)
                .filter(File::isDirectory)
                .forEach(directory -> {
                    SmartNotebook notebook = processDirectory(directory);
                    if (notebook != null) {
                        bootstrappedNotebooks.add(notebook);
                    }
                });

        // Process individual files at the root level
        List<AtomicNoteEntity> rootNotes = new ArrayList<>();
        Arrays.stream(files)
                .filter(File::isFile)
                .forEach(file -> {
                    AtomicNoteEntity note = processFile(file, workingDirectory.getAbsolutePath());
                    if (note != null) {
                        rootNotes.add(note);
                    }
                });

        // If there are any individual notes, create a default notebook for them
        if (!rootNotes.isEmpty()) {
            String defaultNotebookName = "Default Notebook";
            Optional<SmartNotebook> defaultNotebookOpt = smartNotebookRepository.initializeNewSmartNotebook(
                    defaultNotebookName,
                    workingDirectory.getAbsolutePath(),
                    NoteType.NOT_SET);

            if (defaultNotebookOpt.isPresent()) {
                SmartNotebook defaultNotebook = defaultNotebookOpt.get();
                SmartBookEntity smartBookEntity = defaultNotebook.getSmartBook();

                // Add all root notes to this notebook with proper page linking
                List<SmartBookPage> bookPages = new ArrayList<>();
                for (int i = 0; i < rootNotes.size(); i++) {
                    AtomicNoteEntity note = rootNotes.get(i);
                    SmartBookPage page = new SmartBookPage(
                            smartBookEntity.getBookId(),
                            note.getNoteId(),
                            i + 1 // Page order starting from 1 (0 is for the initial note)
                    );

                    // Save the page
                    long pageId = smartBookPagesDao.insertSmartBookPage(page);
                    page.setId(pageId);

                    // Add to the list
                    bookPages.add(page);
                    defaultNotebook.getAtomicNotes().add(note);
                }

                // Add all pages to the notebook
                defaultNotebook.getSmartBookPages().addAll(bookPages);

                // Update the notebook
                smartNotebookRepository.updateNotebook(defaultNotebook, context);
                bootstrappedNotebooks.add(defaultNotebook);
            }
        }

        return bootstrappedNotebooks;
    }

    /**
     * Process a directory as a SmartNotebook
     *
     * @param directory The directory to process
     * @return SmartNotebook if successfully processed, null otherwise
     */
    private SmartNotebook processDirectory(File directory) {
        String notebookName = directory.getName();
        logger.debug("Processing directory as notebook: " + notebookName);

        // Create or get the notebook
        Optional<SmartNotebook> notebookOpt = smartNotebookRepository.initializeNewSmartNotebook(
                notebookName,
                directory.getParentFile().getAbsolutePath(),
                NoteType.NOT_SET);

        if (!notebookOpt.isPresent()) {
            logger.error("Failed to create notebook for directory: " + directory.getAbsolutePath());
            return null;
        }

        SmartNotebook notebook = notebookOpt.get();
        SmartBookEntity smartBookEntity = notebook.getSmartBook();

        // Process files in this directory
        File[] files = directory.listFiles();
        if (files != null) {
            List<SmartBookPage> bookPages = new ArrayList<>();
            List<AtomicNoteEntity> notes = new ArrayList<>();

            int pageOrder = 1; // Start from 1 as 0 is for the initial note
            for (File file : files) {
                if (file.isFile()) {
                    AtomicNoteEntity note = processFile(file, directory.getAbsolutePath());
                    if (note != null) {
                        // Create a page for this note
                        SmartBookPage page = new SmartBookPage(
                                smartBookEntity.getBookId(),
                                note.getNoteId(),
                                pageOrder++
                        );

                        // Save the page
                        long pageId = smartBookPagesDao.insertSmartBookPage(page);
                        page.setId(pageId);

                        // Add to lists
                        bookPages.add(page);
                        notes.add(note);
                    }
                }
            }

            // Add all notes and pages to the notebook
            notebook.getAtomicNotes().addAll(notes);
            notebook.getSmartBookPages().addAll(bookPages);

            // Update the notebook
            smartNotebookRepository.updateNotebook(notebook, null);
        }

        return notebook;
    }

    /**
     * Process a file as an AtomicNoteEntity
     *
     * @param file       The file to process
     * @param parentPath The parent directory path
     * @return AtomicNoteEntity if successfully processed, null otherwise
     */
    private AtomicNoteEntity processFile(File file, String parentPath) {
        String filename = file.getName();
        String filepath = parentPath;
        NoteType noteType = determineNoteType(file);

        logger.debug("Processing file: " + filename + " as " + noteType);

        if (noteType == NoteType.NOT_SET) {
            logger.debug("Skipping unsupported file: " + filename);
            return null;
        }

        String nameWithoutExtension = filename;
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            nameWithoutExtension = filename.substring(0, dotIndex);
        }

        // Create the atomic note entity
        AtomicNoteEntity note = AtomicNotesDomain.constructAtomicNote(
                nameWithoutExtension,
                filepath,
                noteType);

        // Save the note
        return atomicNotesDomain.saveAtomicNote(note);
    }

    /**
     * Determine the type of note based on the file extension
     *
     * @param file The file to analyze
     * @return The determined NoteType
     */
    private NoteType determineNoteType(File file) {
        String filename = file.getName().toLowerCase();

        if (filename.endsWith(".md")) {
            return NoteType.TEXT_NOTE;
        } else if (filename.endsWith(".png") || filename.endsWith(".jpg") ||
                filename.endsWith(".jpeg")) {
            return NoteType.HANDWRITTEN_PNG;
        }

        return NoteType.NOT_SET;
    }
} 