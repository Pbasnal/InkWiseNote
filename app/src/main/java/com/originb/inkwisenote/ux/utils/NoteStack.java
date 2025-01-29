package com.originb.inkwisenote.ux.utils;

import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.modules.repositories.NoteRepository;

import java.util.*;

public class NoteStack {

    private NoteRepository noteRepository;
    private List<NoteEntity> noteStack = new ArrayList<>();
    private Set<Long> insertedNoteIds = new HashSet<>();

    private int currentNoteIndex = -1;

    public NoteStack(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public void setCurrentNote(NoteEntity noteEntity) {
        if (insertedNoteIds.contains(noteEntity.getNoteId())) return;

        if (currentNoteIndex > -1 && currentNoteIndex < noteStack.size() - 1) {
            noteStack.add(currentNoteIndex, noteEntity);
        } else {
            noteStack.add(noteEntity);
            currentNoteIndex = noteStack.size() - 1;
        }
        insertedNoteIds.add(noteEntity.getNoteId());
    }

    public boolean isEmpty() {
        return noteStack.isEmpty();
    }

    public boolean hasNextNote() {
        if (currentNoteIndex == noteStack.size() - 1) {
            findAndInsertNextNotes();
        }
        return currentNoteIndex < noteStack.size() - 1;
    }

    public boolean hasPreviousNote() {
        if (currentNoteIndex <= 0) {
            findAndInsertPrevNotes();
        }
        return currentNoteIndex > 0;
    }

    public Optional<NoteEntity> getCurrentNoteEntity() {
        if (noteStack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(noteStack.get(currentNoteIndex));
        //noteRepository.getNoteEntity(noteStack.get(currentNoteIndex));
    }

    public Optional<NoteEntity> moveToPrevNote() {
        // if the current note was the last note in the stack,
        // find and insert previous notes of current note
        if (noteStack.isEmpty() || currentNoteIndex > 0) {
            findAndInsertPrevNotes();
        }
        // if previous notes were inserted
        if (currentNoteIndex > 0) {
            // move to the prev note
            currentNoteIndex--;
            // find and insert prev notes of the new current note
            // Since one note can have multiple prev notes,
            // this step ensures that we load all the prev notes of the new current note
            // By doing it here, we also ensure that when the note activity renders
            // the new current note, it will show the next and prev buttons correctly
            findAndInsertPrevNotes();
        }
        return Optional.ofNullable(noteStack.get(currentNoteIndex));
    }

    public Optional<NoteEntity> moveToNextNote() {
        // if the current note is the last note in the stack
        if (noteStack.isEmpty() || currentNoteIndex == noteStack.size() - 1) {
            // find and insert next notes of the current note
            findAndInsertNextNotes();
        }
        // if next notes were inserted
        if (currentNoteIndex < noteStack.size() - 1) {
            // move to the next note
            currentNoteIndex++;
            // find and insert next notes of the new current note
            // Since one note can have multiple next notes,
            // this step ensures that we load all the next notes of the new current note
            // By doing it here, we also ensure that when the note activity renders
            // the new current note, it will show the next and prev buttons correctly
            findAndInsertNextNotes();
        }
        return Optional.ofNullable(noteStack.get(currentNoteIndex));

    }

    private void findAndInsertNextNotes() {
        NoteEntity currentNoteEntity = noteStack.get(currentNoteIndex);
        List<NoteEntity> nextNoteEntities = noteRepository.getNextNote(currentNoteEntity.getNoteId());

        nextNoteEntities.stream()
                .filter(noteEntity -> !insertedNoteIds.contains(noteEntity.getNoteId()))
                .forEach(noteEntity -> {
                    noteStack.add(noteEntity);
                    insertedNoteIds.add(noteEntity.getNoteId());
                });
    }

    private void findAndInsertPrevNotes() {
        NoteEntity currentNoteEntity = noteStack.get(currentNoteIndex);
        List<NoteEntity> prevNoteEntities = noteRepository.getPrevNote(currentNoteEntity.getNoteId());
        prevNoteEntities.stream()
                .filter(noteEntity -> !insertedNoteIds.contains(noteEntity.getNoteId()))
                .forEach(noteEntity -> {
                    noteStack.add(0, noteEntity);
                    currentNoteIndex++;
                    insertedNoteIds.add(noteEntity.getNoteId());
                });
    }
}
