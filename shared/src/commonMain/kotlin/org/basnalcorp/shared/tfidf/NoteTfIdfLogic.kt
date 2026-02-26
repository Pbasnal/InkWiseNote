package org.basnalcorp.shared.tfidf

import org.basnalcorp.shared.LogLevel
import org.basnalcorp.shared.PlatformLogger
import org.basnalcorp.shared.data.repository.NoteTermFrequencyRepository
import org.basnalcorp.shared.domain.NoteTermFrequency
import org.basnalcorp.shared.util.MapsUtils
import kotlin.math.ln

/**
 * TF-IDF and term-document operations in commonMain.
 * Uses [NoteTermFrequencyRepository] and [PlatformLogger].
 */
class NoteTfIdfLogic(
    private val noteTermFrequencyRepository: NoteTermFrequencyRepository,
    private val logger: PlatformLogger
) {
    private val tag = "NoteTfIdfLogic"

    fun addOrUpdateNote(noteId: Long, termsList: List<String>) {
        val termFrequenciesOfNote = toTermFrequencyMap(noteTermFrequencyRepository.getTermFrequenciesForNote(noteId))
        if (termFrequenciesOfNote.isEmpty()) {
            logger.log(LogLevel.DEBUG, tag, "Insert document terms for noteId: $noteId")
            insertDocument(noteId, termsList)
        } else {
            logger.log(LogLevel.DEBUG, tag, "Update document terms for noteId: $noteId")
            updateDocument(noteId, termFrequenciesOfNote, termsList)
        }
    }

    fun updateDocument(noteId: Long, oldTermFrequenciesOfNote: Map<String, Long>, newTerms: List<String>) {
        noteTermFrequencyRepository.deleteForNote(noteId)
        insertDocument(noteId, newTerms)
    }

    fun deleteDocument(noteId: Long) {
        val termFrequenciesOfNote = toTermFrequencyMap(noteTermFrequencyRepository.getTermFrequenciesForNote(noteId))
        if (termFrequenciesOfNote.isEmpty()) return
        noteTermFrequencyRepository.deleteForNote(noteId)
    }

    fun getTfIdf(noteId: Long): Map<String, Double> {
        val tfIdfScores = mutableMapOf<String, Double>()
        val termFrequenciesOfNote = toTermFrequencyMap(noteTermFrequencyRepository.getTermFrequenciesForNote(noteId))
        if (MapsUtils.isEmpty(termFrequenciesOfNote)) return tfIdfScores
        val n = noteTermFrequencyRepository.getDistinctNoteIdCount()
        val termIdfScores = calculateIdf(termFrequenciesOfNote.keys, n.toInt())
        if (MapsUtils.isEmpty(termIdfScores)) return tfIdfScores
        val totalTerms = termFrequenciesOfNote.values.sum()
        for ((term, tf) in termFrequenciesOfNote) {
            val idf = termIdfScores[term] ?: 0.0
            tfIdfScores[term] = (tf / totalTerms.toDouble()) * idf
        }
        return tfIdfScores
    }

    fun getRelatedDocuments(terms: Set<String?>): Map<String, Set<Long>> {
        val safeTerms = terms.filterNotNull().toSet()
        if (safeTerms.isEmpty()) return emptyMap()
        return noteTermFrequencyRepository.getNoteIdsForTerms(safeTerms)
    }

    private fun calculateIdf(terms: Set<String>, n: Int): Map<String, Double> {
        if (terms.isEmpty()) return emptyMap()
        val termNoteIds = noteTermFrequencyRepository.getNoteIdsForTerms(terms)
        val termDf = termNoteIds.mapValues { it.value.size }
        return termDf.mapValues { (_, df) -> if (df > 0) ln(n.toDouble() / df) else 0.0 }
    }

    private fun toTermFrequencyMap(entries: List<NoteTermFrequency>): Map<String, Long> {
        val map = mutableMapOf<String, Long>()
        for (e in entries) {
            map[e.term] = e.termFrequency
        }
        return map
    }

    private fun insertDocument(noteId: Long, termsList: List<String>) {
        val termCounts = mutableMapOf<String, Long>()
        for (term in termsList) {
            termCounts[term] = (termCounts[term] ?: 0) + 1
        }
        val entries = termCounts.map { (term, count) ->
            NoteTermFrequency(noteId = noteId, term = term, termFrequency = count)
        }
        noteTermFrequencyRepository.insertTermFrequencies(entries)
    }
}
