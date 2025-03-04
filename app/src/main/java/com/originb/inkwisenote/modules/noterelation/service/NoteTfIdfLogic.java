package com.originb.inkwisenote.modules.noterelation.service;

import android.util.Log;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote.common.Logger;
import com.originb.inkwisenote.modules.ocr.data.NoteTermFrequencyDao;
import com.originb.inkwisenote.modules.ocr.data.NoteTermFrequency;
import com.originb.inkwisenote.modules.ocr.data.TermOccurrence;
import com.originb.inkwisenote.common.MapsUtils;
import com.originb.inkwisenote.modules.repositories.Repositories;

import java.util.*;

public class NoteTfIdfLogic {

    private Logger logger = new Logger("NoteTfIdfLogic");

    private NoteTermFrequencyDao noteTermFrequencyDao;

    public NoteTfIdfLogic(Repositories repositories) {
        noteTermFrequencyDao = repositories.getNotesDb().noteTermFrequencyDao();
    }

    public void addOrUpdateNote(Long noteId, List<String> termsList) {
        Map<String, Integer> termFrequenciesOfNote = toTermFrequencyMap(noteTermFrequencyDao.readTermFrequenciesOfNote(noteId));

        if (termFrequenciesOfNote.isEmpty()) {
            logger.debug("Insert document terms for noteId: " + noteId, termsList);
            insertDocument(noteId, termsList);
        } else {
            logger.debug("Update document terms for noteId: " + noteId, termsList);
            updateDocument(noteId, termFrequenciesOfNote, termsList);
        }
    }

    // IDF is calculated by dividing the total number of documents
    // by the number of documents in the collection containing the term.
    private Map<String, Double> calculateIdf(Set<String> terms, int N) {
        Map<String, Integer> termDf = toTermOccuranceMap(noteTermFrequencyDao.getTermOccurrences(terms));
        Map<String, Double> termIdfScore = new HashMap<>();
        for (String term : termDf.keySet()) {
            int df = termDf.getOrDefault(term, 0);
            Log.d("Value of df", "" + df);
            double idf = df > 0 ? Math.log((double) N / df) : 0;
            termIdfScore.putIfAbsent(term, idf);
        }
        return termIdfScore;
    }

    private Map<String, Integer> toTermOccuranceMap(List<TermOccurrence> termOccurrences) {
        Map<String, Integer> termFrequenciesOfNote = new HashMap<>();
        for (TermOccurrence termOccurrence : termOccurrences) {
            termFrequenciesOfNote.put(termOccurrence.getTerm(), termOccurrence.getOccurrenceCount());
        }
        return termFrequenciesOfNote;
    }

    private Map<String, Integer> toTermFrequencyMap(List<NoteTermFrequency> noteTermFrequencies) {
        Map<String, Integer> termFrequenciesOfNote = new HashMap<>();
        if (CollectionUtils.isEmpty(noteTermFrequencies)) return termFrequenciesOfNote;
        for (NoteTermFrequency noteTermFrequency : noteTermFrequencies) {
            termFrequenciesOfNote.put(noteTermFrequency.getTerm(), noteTermFrequency.getTermFrequency());
        }
        return termFrequenciesOfNote;
    }


    private void insertDocument(Long noteId, List<String> termsList) {
        Map<String, NoteTermFrequency> termFrequencies = new HashMap<>();
        for (String term : termsList) {
            NoteTermFrequency termFq = termFrequencies.getOrDefault(term, new NoteTermFrequency(noteId, term, 0));
            termFq.setTermFrequency(termFq.getTermFrequency() + 1);
            termFrequencies.put(term, termFq);
        }

        noteTermFrequencyDao.insertTermFrequenciesToDb(new ArrayList<>(termFrequencies.values()));
    }

    public void updateDocument(Long noteId,
                               Map<String, Integer> oldTermFrequenciesOfNote,
                               List<String> newTerms) {

        Set<String> oldTerms = oldTermFrequenciesOfNote.keySet();
        Set<String> removedTerms = new HashSet<>(oldTerms);
        removedTerms.removeAll(newTerms);
        Set<String> addedTerms = new HashSet<>(newTerms);
        addedTerms.removeAll(oldTerms);

        // clearing out all existing term frequencies
        // update repeatedTerms, insert addedTerms and delete removed terms - 3 queries
        // delete old terms and insert new - 2 queries
        noteTermFrequencyDao.deleteTermFrequencies(noteId);
        insertDocument(noteId, newTerms);
    }

    public void deleteDocument(Long noteId) {
        Map<String, Integer> termFrequenciesOfNote = toTermFrequencyMap(noteTermFrequencyDao.readTermFrequenciesOfNote(noteId));
        if (Objects.isNull(termFrequenciesOfNote) || termFrequenciesOfNote.isEmpty()) return;

        noteTermFrequencyDao.deleteTermFrequencies(noteId);

        // todo: is this needed?
        int N = noteTermFrequencyDao.getDistinctNoteIdCount();
        Map<String, Double> termIdfScores = calculateIdf(termFrequenciesOfNote.keySet(), N);
    }

    public Map<String, Double> getTfIdf(Long noteId) {
        Map<String, Double> tfIdfScores = new HashMap<>();

        Map<String, Integer> termFrequenciesOfNote = toTermFrequencyMap(noteTermFrequencyDao.readTermFrequenciesOfNote(noteId));
        if (MapsUtils.isEmpty(termFrequenciesOfNote)) return tfIdfScores;

        int N = noteTermFrequencyDao.getDistinctNoteIdCount();
        Map<String, Double> termIdfScores = calculateIdf(termFrequenciesOfNote.keySet(), N);

        if (MapsUtils.isEmpty(termIdfScores)) return tfIdfScores;

        int totalTerms = termFrequenciesOfNote.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : termFrequenciesOfNote.entrySet()) {
            String term = entry.getKey();
            int tf = entry.getValue();
            Double idf = termIdfScores.getOrDefault(term, 0.0);
            tfIdfScores.put(term, (tf / (double) totalTerms) * idf);
        }

        return tfIdfScores;
    }

    public Map<String, Set<Long>> getRelatedDocuments(Set<String> terms) {
        Map<String, Set<Long>> termNoteIds = toTermNoteIdsMap(noteTermFrequencyDao.getNoteIdsForTerms(terms));
        if (MapsUtils.isEmpty(termNoteIds)) return new HashMap<>();
        return termNoteIds;
    }

    private Map<String, Set<Long>> toTermNoteIdsMap(List<NoteTermFrequency> termFrequencies) {
        Map<String, Set<Long>> termNoteIds = new HashMap<>();
        for (NoteTermFrequency noteTermFrequency : termFrequencies) {
            Set<Long> noteIds = termNoteIds.getOrDefault(noteTermFrequency.getTerm(), new HashSet<>());
            noteIds.add(noteTermFrequency.getNoteId());
            termNoteIds.put(noteTermFrequency.getTerm(), noteIds);
        }
        return termNoteIds;
    }

    // Example usage
//    public static void main(String[] args) {
//        BiRelationalGraph brGraph = new BiRelationalGraph();
//
//        // Add documents
//        brGraph.addDocument("1", Arrays.asList("apple", "banana", "apple", "orange"));
//        brGraph.addDocument("2", Arrays.asList("banana", "orange", "kiwi", "apple"));
//        brGraph.addDocument("3", Arrays.asList("orange", "banana", "banana"));
//
//        // Print TF-IDF for document 1
//        System.out.println("TF-IDF for Document 1: " + brGraph.getTfIdf("1"));
//
//        // Update document 2 with new terms
//        brGraph.updateDocument("2", Arrays.asList("apple", "kiwi", "banana", "grape"));
//
//        // Print updated TF-IDF for document 2
//        System.out.println("Updated TF-IDF for Document 2: " + brGraph.getTfIdf("2"));
//
//        // Delete document 3
//        brGraph.deleteDocument("3");
//
//        // Check TF-IDF after deletion
//        System.out.println("TF-IDF for Document 1 after deleting Document 3: " + brGraph.getTfIdf("1"));
//
//        // Retrieve documents related to the term 'apple'
//        System.out.println("Documents containing 'apple': " + brGraph.getRelatedDocuments("apple"));
//    }
}
