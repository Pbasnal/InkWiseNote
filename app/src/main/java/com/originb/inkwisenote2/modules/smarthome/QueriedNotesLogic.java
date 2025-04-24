package com.originb.inkwisenote2.modules.smarthome;

import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency;
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao;
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class QueriedNotesLogic {
    public static final int IGNORE = 0;
    public static final int ONLY_IGNORE_WORDS = 1;
    public static final int ONLY_FIND_WORDS = 2;
    public static final int FIND_AND_FILTER = 3;

    public static Set<String> splitAndSanitize(String wordsStr) {
        String[] wordsArr = wordsStr.split(",");
        Set<String> wordsSet = new HashSet<>();
        if (wordsArr != null) {
            for (int i = 0; i < wordsArr.length; i++) {
                wordsSet.add(wordsArr[i].trim());
            }
        }
        return wordsSet;
    }

    public static int getQueryScenario(Set<String> wordsToFind, Set<String> wordsToIgnore) {
        if (wordsToFind == null || wordsToFind.isEmpty()) {
            if (wordsToIgnore == null || wordsToIgnore.isEmpty()) {
                return IGNORE;
            } else {
                return ONLY_IGNORE_WORDS;
            }
        } else if (wordsToIgnore == null || wordsToIgnore.isEmpty()) {
            return ONLY_FIND_WORDS;
        }
        return FIND_AND_FILTER;
    }

    public static List<AtomicNoteEntity> getNotesThatHaveWords(
            Set<String> wordsToFind,
            NoteTermFrequencyDao noteTermFrequencyDao,
            AtomicNotesDomain atomicNotesDomain,
            Optional<Long> bookId) {

        List<NoteTermFrequency> noteTermFrequencies = noteTermFrequencyDao.getNoteIdsForTerms(wordsToFind);
        Set<Long> noteIds = noteTermFrequencies.stream()
                .map(NoteTermFrequency::getNoteId)
                .collect(Collectors.toSet());
        List<AtomicNoteEntity> atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIds);

        return atomicNoteEntities;
    }

    // hopefully this will not be used a lot
    public static List<AtomicNoteEntity> getNotesThatDontHaveWords(
            Set<String> wordsToIgnore,
            NoteTermFrequencyDao noteTermFrequencyDao,
            AtomicNotesDomain atomicNotesDomain,
            Optional<Long> bookId) {
        List<NoteTermFrequency> noteTermFrequencies = noteTermFrequencyDao.getNoteIdsForAllTermsExcept(wordsToIgnore);
        Set<Long> noteIds = noteTermFrequencies.stream()
                .map(NoteTermFrequency::getNoteId)
                .collect(Collectors.toSet());
        List<AtomicNoteEntity> atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIds);
        return atomicNoteEntities;
    }

    public static List<AtomicNoteEntity> getNotesWithWordsAndFilter(
            Set<String> wordsToFind,
            Set<String> wordsToIgnore,
            NoteTermFrequencyDao noteTermFrequencyDao,
            AtomicNotesDomain atomicNotesDomain,
            Optional<Long> bookId) {
        List<NoteTermFrequency> noteTermFrequencies = noteTermFrequencyDao.getNoteIdsForTerms(wordsToFind);
        Set<Long> noteIds = noteTermFrequencies.stream()
                .filter(tf -> !wordsToIgnore.contains(tf.getTerm()))
                .map(NoteTermFrequency::getNoteId)
                .collect(Collectors.toSet());
        List<AtomicNoteEntity> atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIds);

        return atomicNoteEntities;
    }
}
