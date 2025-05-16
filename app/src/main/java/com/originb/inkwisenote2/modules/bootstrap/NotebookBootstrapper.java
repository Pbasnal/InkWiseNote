package com.originb.inkwisenote2.modules.bootstrap;

import android.content.Context;

import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote2.common.ListUtils;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.common.Strings;

import java.io.File;
import java.util.*;

/**
 * Class for bootstrapping the app by scanning the working directory for notebooks and notes
 */
public class NotebookBootstrapper {
    private static final String TAG = "NotebookBootstrapper";
    private final Logger logger = new Logger(TAG);

    private Map<String, NotebookFolder> smartBookFolders = new HashMap<>();
    private Map<String, NoteFileInfo> notesOnRootFolder = new HashMap<>();

    public NotebookBootstrapper() {
    }

    /**
     * Scan the working directory for notebooks and notes
     *
     * @param context          Android context
     * @param workingDirectory The directory to scan
     * @return List of bootstrapped SmartNotebooks
     */
    public List<NotebookFolder> bootstrapFromDirectory(Context context, File workingDirectory) {
        List<NotebookFolder> bootstrappedNotebooks = new ArrayList<>();

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
                    NotebookFolder notebook = processDirectory(directory);
                    if (notebook != null) {
                        bootstrappedNotebooks.add(notebook);
                    }
                });

        // Process individual files at the root level
        List<NoteFileInfo> rootNotes = new ArrayList<>();
        Arrays.stream(files)
                .filter(File::isFile)
                .forEach(file -> {
                    NoteFileInfo note = processFile(file, workingDirectory.getAbsolutePath());
                    if (note != null) {
                        rootNotes.add(note);
                    }
                });

        return bootstrappedNotebooks;
    }

    /**
     * Process a directory as a SmartNotebook
     *
     * @param directory The directory to process
     * @return SmartNotebook if successfully processed, null otherwise
     */
    private NotebookFolder processDirectory(File directory) {
        String notebookName = directory.getName();
        logger.debug("Processing directory as notebook: " + notebookName);

        NotebookFolder notebookFolder = new NotebookFolder();
        notebookFolder.notebookName = notebookName;

        // Process files in this directory
        File[] files = directory.listFiles();

        if (files == null || files.length == 0) {
            return notebookFolder;
        }

        for (File file : files) {
            if (!file.isFile()) {
                // Todo: Notebooks inside notebooks?
                continue;
            }

            NoteFileInfo note = processFile(file, directory.getAbsolutePath());

            if (note == null || note.fileType == NoteFileTypes.UNRECOGNIZED) {
                continue;
            }

            if (CollectionUtils.isEmpty(notebookFolder.fileInfos.get(note.notename))) {
                notebookFolder.fileInfos.put(note.notename, ListUtils.listOf(note));
            } else {
                notebookFolder.fileInfos.get(note.notename).add(note);
            }
        }

        return notebookFolder;
    }

    /**
     * Process a file as an AtomicNoteEntity
     *
     * @param file       The file to process
     * @param parentPath The parent directory path
     * @return AtomicNoteEntity if successfully processed, null otherwise
     */
    private NoteFileInfo processFile(File file, String parentPath) {
        if (file == null || Strings.isNullOrWhitespace(parentPath)) return null;

        String filename = file.getName();

        String[] splittedString = filename.toLowerCase().split("\\.");
        if (splittedString.length == 0) {
            return new NoteFileInfo(NoteFileTypes.UNRECOGNIZED, file.getPath(), filename);
        }
        String fileExtension = splittedString[splittedString.length - 1];
        switch (fileExtension) {
            case "md": // markdown text or handwritten note
                return new NoteFileInfo(NoteFileTypes.TEXT_NOTE, file.getPath(), filename);
            case "png": // handwritten note image or thumbnail
                return new NoteFileInfo(NoteFileTypes.HANDWRITTEN_PNG, file.getPath(), filename);
            case "pt": // page template
                return new NoteFileInfo(NoteFileTypes.PAGE_TEMPLATE, file.getPath(), filename);
        }
        return new NoteFileInfo(NoteFileTypes.UNRECOGNIZED, file.getPath(), filename);
    }

    public static class NoteFileTypes {
        public static int UNRECOGNIZED = 0;
        public static int HANDWRITTEN_NOTE = 1;
        public static int TEXT_NOTE = 2;
        public static int PAGE_TEMPLATE = 3;
        public static int HANDWRITTEN_PNG = 4;
    }

    public class NoteFileInfo {
        public String filepath;
        public String filename;

        public String notename;

        public int fileType;

        public NoteFileInfo(int fileType, String filepath, String filename) {
            this.filepath = filepath;
            this.filename = filename;
            this.fileType = fileType;
            this.notename = filename;
        }

        public NoteFileInfo(int fileType, String notename, String filepath, String filename) {
            this.filepath = filepath;
            this.filename = filename;
            this.fileType = fileType;
            this.notename = notename;
        }
    }

    public class NotebookFolder {
        public Map<String, List<NoteFileInfo>> fileInfos = new HashMap<>();
        public String notebookName;
    }
} 