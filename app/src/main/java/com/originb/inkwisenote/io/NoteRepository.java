package com.originb.inkwisenote.io;

import com.originb.inkwisenote.constants.Returns;
import com.originb.inkwisenote.data.Note;
import com.originb.inkwisenote.filemanager.JsonFileManager;

import java.io.*;
import java.util.*;

public class NoteRepository {
    private final File directory;

    private Map<Long, Note> notes;
    private Long[] noteIds;
    private Map<Long, String> noteIdToNameMap;
    private Map<Long, String> noteIdToNotePath;

    // Constructor to set the directory where notes will be saved
    public NoteRepository(File directory) {
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

        String noteName = noteIdToNotePath.get(noteId);
        File noteFile = new File(noteName);
        noteFile.delete();

        noteIdToNotePath.remove(noteId);
        noteIdToNameMap.remove(noteId);
        notes.remove(noteId);
    }

    public Note getNoteAtIndex(int position) {
        return notes.get(noteIds[position]);
    }

    public Optional<Note> getNote(Long noteId) {
        if (Objects.isNull(noteId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(notes.get(noteId));
    }

    public String getPathOfNote(Long noteId) {
        return noteIdToNotePath.get(noteId);
    }

    public static Note createNewNote() {
        Long noteId = System.currentTimeMillis();
        String noteName = "note-" + noteId;

        Note note = new Note(noteId);
        note.setNoteName(noteName);
        return note;
    }

    public Returns saveNote(String noteDirectory, Long noteId, Note note) {
        if (Objects.isNull(note)) {
            return Returns.INVALID_ARGUMENTS;
        }
        if (notes.containsKey(noteId) || noteIdToNameMap.containsKey(noteId) || noteIdToNotePath.containsKey(noteId)) {
            return Returns.NOTE_DOESNT_EXISTS;
        }

        String noteFullPath = noteDirectory + "/" + note.getNoteName() + ".note";
        JsonFileManager.writeDataToDisk(noteFullPath, note);

        notes.put(noteId, note);
        noteIdToNameMap.put(noteId, note.getNoteName());
        noteIdToNotePath.put(noteId, noteDirectory);
        return Returns.SUCCESS;
    }

    public Returns updateNoteMeta(Long noteId, Note note) {
        if (Objects.isNull(note)) {
            return Returns.INVALID_ARGUMENTS;
        }
        if (!notes.containsKey(noteId)) {
            return Returns.NOTE_DOESNT_EXISTS;
        }
        notes.put(noteId, note);
        String notePath = noteIdToNotePath.get(noteId);
        JsonFileManager.writeDataToDisk(notePath, note);
        return Returns.SUCCESS;
    }

    public void loadAll() {
        File[] noteFiles = directory.listFiles((dir, name) -> name.endsWith(".note"));

        if (Objects.isNull(noteFiles)) return;

        notes = new HashMap<>();
        noteIdToNameMap = new HashMap<>();
        noteIdToNotePath = new HashMap<>();
        for (int i = 0; i < noteFiles.length; i++) {
            File noteFile = noteFiles[i];
            String noteNameWithoutExtension = noteFile.getName();
            noteNameWithoutExtension = noteNameWithoutExtension.substring(0, noteNameWithoutExtension.lastIndexOf('.'));
            Long noteId = parseNoteIdFromFileName(noteNameWithoutExtension);
            if (Objects.isNull(noteId)) continue;

            String notePath = noteFile.getPath().replace("/" + noteFile.getName(), "");

            JsonFileManager.readDataFromDisk(noteFiles[i].getPath(), Note.class)
                    .ifPresent(n -> {
                        n.setNoteId(noteId);
                        notes.put(noteId, n);
                        noteIdToNameMap.put(noteId, n.getNoteName());
                        noteIdToNotePath.put(noteId, notePath);
                    });
        }

        noteIds = noteIdToNameMap.keySet().toArray(new Long[0]);
    }

    private boolean noteExists(Long noteId) {
        return notes.containsKey(noteId) || noteIdToNotePath.containsKey(noteId) || noteIdToNameMap.containsKey(noteId);
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