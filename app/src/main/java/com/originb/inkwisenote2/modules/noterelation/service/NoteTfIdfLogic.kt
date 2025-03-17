package com.originb.inkwisenote2.modules.noterelation.service

import android.util.Log
import com.google.android.gms.common.util.CollectionUtils
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.common.MapsUtils
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import com.originb.inkwisenote2.modules.ocr.data.TermOccurrence
import com.originb.inkwisenote2.modules.repositories.Repositories
import java.util.*
import kotlin.math.ln

class NoteTfIdfLogic(repositories: Repositories?) {
    private val logger = Logger("NoteTfIdfLogic")

    private val noteTermFrequencyDao: NoteTermFrequencyDao = repositories.getNotesDb().noteTermFrequencyDao()

    fun addOrUpdateNote(noteId: Long?, termsList: List<String?>?) {
        val termFrequenciesOfNote = toTermFrequencyMap(noteTermFrequencyDao.readTermFrequenciesOfNote(noteId))

        if (termFrequenciesOfNote.isEmpty()) {
            logger.debug("Insert document terms for noteId: $noteId", termsList)
            insertDocument(noteId!!, termsList)
        } else {
            logger.debug("Update document terms for noteId: $noteId", termsList)
            updateDocument(noteId, termFrequenciesOfNote, termsList)
        }
    }

    // IDF is calculated by dividing the total number of documents
    // by the number of documents in the collection containing the term.
    private fun calculateIdf(terms: Set<String?>, N: Int): Map<String?, Double> {
        val termDf = toTermOccuranceMap(noteTermFrequencyDao.getTermOccurrences(terms))
        val termIdfScore: MutableMap<String?, Double> = HashMap()
        for (term in termDf.keys) {
            val df = termDf.getOrDefault(term, 0)
            Log.d("Value of df", "" + df)
            val idf: Double = if (df > 0) ln(N.toDouble() / df) else 0
            termIdfScore.putIfAbsent(term, idf)
        }
        return termIdfScore
    }

    private fun toTermOccuranceMap(termOccurrences: List<TermOccurrence?>?): Map<String, Int> {
        val termFrequenciesOfNote: MutableMap<String, Int> = HashMap()
        for (termOccurrence in termOccurrences!!) {
            termFrequenciesOfNote[termOccurrence.getTerm()] = termOccurrence.getOccurrenceCount()
        }
        return termFrequenciesOfNote
    }

    private fun toTermFrequencyMap(noteTermFrequencies: List<NoteTermFrequency?>?): Map<String?, Int> {
        val termFrequenciesOfNote: MutableMap<String?, Int> = HashMap()
        if (CollectionUtils.isEmpty(noteTermFrequencies)) return termFrequenciesOfNote
        for (noteTermFrequency in noteTermFrequencies!!) {
            termFrequenciesOfNote[noteTermFrequency.getTerm()] = noteTermFrequency.getTermFrequency()
        }
        return termFrequenciesOfNote
    }


    private fun insertDocument(noteId: Long, termsList: List<String?>?) {
        val termFrequencies: MutableMap<String?, NoteTermFrequency> = HashMap()
        for (term in termsList!!) {
            val termFq = termFrequencies.getOrDefault(term, NoteTermFrequency(noteId, term, 0))
            termFq.termFrequency = termFq.termFrequency + 1
            termFrequencies[term] = termFq
        }

        noteTermFrequencyDao.insertTermFrequenciesToDb(ArrayList(termFrequencies.values))
    }

    fun updateDocument(
        noteId: Long?,
        oldTermFrequenciesOfNote: Map<String?, Int>,
        newTerms: List<String?>?
    ) {
        val oldTerms = oldTermFrequenciesOfNote.keys
        val removedTerms: MutableSet<String?> = HashSet(oldTerms)
        removedTerms.removeAll(newTerms!!)
        val addedTerms: MutableSet<String?> = HashSet(newTerms)
        addedTerms.removeAll(oldTerms)

        // clearing out all existing term frequencies
        // update repeatedTerms, insert addedTerms and delete removed terms - 3 queries
        // delete old terms and insert new - 2 queries
        noteTermFrequencyDao.deleteTermFrequencies(noteId)
        insertDocument(noteId!!, newTerms)
    }

    fun deleteDocument(noteId: Long?) {
        val termFrequenciesOfNote = toTermFrequencyMap(noteTermFrequencyDao.readTermFrequenciesOfNote(noteId))
        if (Objects.isNull(termFrequenciesOfNote) || termFrequenciesOfNote.isEmpty()) return

        noteTermFrequencyDao.deleteTermFrequencies(noteId)

        // todo: is this needed?
        val N = noteTermFrequencyDao.distinctNoteIdCount
        val termIdfScores = calculateIdf(termFrequenciesOfNote.keys, N)
    }

    fun getTfIdf(noteId: Long?): Map<String?, Double> {
        val tfIdfScores: MutableMap<String?, Double> = HashMap()

        val termFrequenciesOfNote = toTermFrequencyMap(noteTermFrequencyDao.readTermFrequenciesOfNote(noteId))
        if (MapsUtils.isEmpty(termFrequenciesOfNote)) return tfIdfScores

        val N = noteTermFrequencyDao.distinctNoteIdCount
        val termIdfScores = calculateIdf(termFrequenciesOfNote.keys, N)

        if (MapsUtils.isEmpty(termIdfScores)) return tfIdfScores

        val totalTerms = termFrequenciesOfNote.values.stream().mapToInt { obj: Int -> obj.toInt() }.sum()

        for ((term, tf) in termFrequenciesOfNote) {
            val idf = termIdfScores.getOrDefault(term, 0.0)
            tfIdfScores[term] = tf / totalTerms.toDouble() * idf
        }

        return tfIdfScores
    }

    fun getRelatedDocuments(terms: Set<String?>?): Map<String, MutableSet<Long>> {
        val termNoteIds = toTermNoteIdsMap(noteTermFrequencyDao.getNoteIdsForTerms(terms))
        if (MapsUtils.isEmpty(termNoteIds)) return HashMap()
        return termNoteIds
    }

    private fun toTermNoteIdsMap(termFrequencies: List<NoteTermFrequency?>?): Map<String, MutableSet<Long>> {
        val termNoteIds: MutableMap<String, MutableSet<Long>> = HashMap()
        for (noteTermFrequency in termFrequencies!!) {
            val noteIds = termNoteIds.getOrDefault(noteTermFrequency.getTerm(), HashSet())
            noteIds.add(noteTermFrequency.getNoteId())
            termNoteIds[noteTermFrequency.getTerm()] = noteIds
        }
        return termNoteIds
    } // Example usage
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
