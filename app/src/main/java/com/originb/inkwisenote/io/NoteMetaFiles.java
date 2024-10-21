package com.originb.inkwisenote.io;

import com.originb.inkwisenote.constants.Returns;
import com.originb.inkwisenote.data.NoteMeta;
import com.originb.inkwisenote.io.utils.BytesFileIoUtils;

import java.io.*;
import java.util.*;

public class NoteMetaFiles {
    private final File directory;

    private Map<Long, NoteMeta> notes;
    private Long[] noteIds;
    private Map<Long, String> noteIdToNotePath;

    // Constructor to set the directory where notes will be saved
    public NoteMetaFiles(File directory) {
        this.directory = directory;
        // Ensure the directory exists
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void deleteNoteFromDisk(Long noteId) {
        if (Objects.isNull(noteId)) {
            return;
        }
        if (!noteExists(noteId)) {
            return;
        }

        NoteMeta noteMeta = notes.get(noteId);
        String noteFullPath = noteIdToNotePath.get(noteId) + "/" + noteMeta.getNoteFileName() + ".note";
        File noteFile = new File(noteFullPath);
        noteFile.delete();

        noteIdToNotePath.remove(noteId);
        notes.remove(noteId);
        noteIds = notes.keySet().toArray(new Long[0]);
    }

    public NoteMeta getNoteAtIndex(int position) {
        return notes.get(noteIds[position]);
    }

    public Optional<NoteMeta> getNote(Long noteId) {
        if (Objects.isNull(noteId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(notes.get(noteId));
    }

    public String getDirectoryOfNote(Long noteId) {
        return noteIdToNotePath.get(noteId);
    }

    public static NoteMeta createNewNote(String noteTitle) {
        Long noteId = System.currentTimeMillis();
        String noteName = "note-" + noteId;

        NoteMeta noteMeta = new NoteMeta(noteId);
        noteMeta.setNoteFileName(noteName);
        noteMeta.setNoteTitle(noteTitle);
        noteMeta.setCreatedTimeMillis(noteId);
        noteMeta.setLastModifiedTimeMillis(noteId);
        return noteMeta;
    }

    public Returns saveNote(String noteDirectory, Long noteId, NoteMeta noteMeta) {
        if (Objects.isNull(noteMeta)) {
            return Returns.INVALID_ARGUMENTS;
        }
        if (notes.containsKey(noteId)
                || noteIdToNotePath.containsKey(noteId)) {
            return Returns.NOTE_DOESNT_EXISTS;
        }

        String noteFullPath = noteDirectory + "/" + noteMeta.getNoteFileName() + ".note";
        BytesFileIoUtils.writeDataToDisk(noteFullPath, noteMeta);

        notes.put(noteId, noteMeta);
        noteIdToNotePath.put(noteId, noteDirectory);
        return Returns.SUCCESS;
    }

    public Returns updateNoteMeta(Long noteId, NoteMeta noteMeta) {
        if (Objects.isNull(noteMeta)) {
            return Returns.INVALID_ARGUMENTS;
        }
        if (!notes.containsKey(noteId)) {
            return Returns.NOTE_DOESNT_EXISTS;
        }

        noteMeta.setLastModifiedTimeMillis(System.currentTimeMillis());

        notes.put(noteId, noteMeta);
        String noteFullPath = noteIdToNotePath.get(noteId) + "/" + noteMeta.getNoteFileName() + ".note";
        BytesFileIoUtils.writeDataToDisk(noteFullPath, noteMeta);
        return Returns.SUCCESS;
    }

    public Long[] getAllNoteIds() {
        return noteIds;
    }

    public void loadAll() {
        File[] noteFiles = directory.listFiles((dir, name) -> name.endsWith(".note"));

        if (Objects.isNull(noteFiles)) return;

        notes = new HashMap<>();
        noteIdToNotePath = new HashMap<>();
        for (int i = 0; i < noteFiles.length; i++) {
            File noteFile = noteFiles[i];
            String noteNameWithoutExtension = noteFile.getName();
            noteNameWithoutExtension = noteNameWithoutExtension.substring(0, noteNameWithoutExtension.lastIndexOf('.'));
            Long noteId = parseNoteIdFromFileName(noteNameWithoutExtension);
            if (Objects.isNull(noteId)) continue;

            String notePath = noteFile.getPath().replace("/" + noteFile.getName(), "");

            BytesFileIoUtils.readDataFromDisk(noteFiles[i].getPath(), NoteMeta.class)
                    .ifPresent(n -> {
                        n.setNoteId(noteId);
                        notes.put(noteId, n);
                        noteIdToNotePath.put(noteId, notePath);
                    });
        }

        noteIds = notes.keySet().toArray(new Long[0]);
    }

    private boolean noteExists(Long noteId) {
        return notes.containsKey(noteId)
                || noteIdToNotePath.containsKey(noteId);
    }

    private static Long parseNoteIdFromFileName(String noteNameWithoutExtension) {
        if (noteNameWithoutExtension.contains("-")) {
            return Long.parseLong(noteNameWithoutExtension.split("-")[1]);
        } else if (noteNameWithoutExtension.contains("_")) {
            return Long.parseLong(noteNameWithoutExtension.split("_")[1]);
        }
        return null;
    }


    public int numberOfNotes() {
        return notes.size();
    }
}