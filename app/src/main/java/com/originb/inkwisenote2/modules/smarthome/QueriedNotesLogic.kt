package com.originb.inkwisenote2.modules.smarthome

import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import java.util.*
import java.util.stream.Collectors

object QueriedNotesLogic {
    const val IGNORE: Int = 0
    const val ONLY_IGNORE_WORDS: Int = 1
    const val ONLY_FIND_WORDS: Int = 2
    const val FIND_AND_FILTER: Int = 3

    fun splitAndSanitize(wordsStr: String): MutableSet<String?> {
        val wordsArr: Array<String?> = wordsStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val wordsSet: MutableSet<String?> = HashSet<String?>()
        if (wordsArr != null) {
            for (i in wordsArr.indices) {
                wordsSet.add(wordsArr[i]!!.trim { it <= ' ' })
            }
        }
        return wordsSet
    }

    fun getQueryScenario(wordsToFind: MutableSet<String?>?, wordsToIgnore: MutableSet<String?>?): Int {
        if (wordsToFind == null || wordsToFind.isEmpty()) {
            if (wordsToIgnore == null || wordsToIgnore.isEmpty()) {
                return IGNORE
            } else {
                return ONLY_IGNORE_WORDS
            }
        } else if (wordsToIgnore == null || wordsToIgnore.isEmpty()) {
            return ONLY_FIND_WORDS
        }
        return FIND_AND_FILTER
    }

    fun getNotesThatHaveWords(
        wordsToFind: MutableSet<String?>?,
        noteTermFrequencyDao: NoteTermFrequencyDao,
        atomicNotesDomain: AtomicNotesDomain,
        bookId: Optional<Long?>?
    ): MutableList<AtomicNoteEntity?>? {
        val noteTermFrequencies = noteTermFrequencyDao.getNoteIdsForTerms(wordsToFind)
        val noteIds: MutableSet<Long?> = noteTermFrequencies!!.stream()
            .map<Any?>(NoteTermFrequency::getNoteId)
            .collect(Collectors.toSet())
        val atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIds)

        return atomicNoteEntities
    }

    // hopefully this will not be used a lot
    fun getNotesThatDontHaveWords(
        wordsToIgnore: MutableSet<String?>?,
        noteTermFrequencyDao: NoteTermFrequencyDao,
        atomicNotesDomain: AtomicNotesDomain,
        bookId: Optional<Long?>?
    ): MutableList<AtomicNoteEntity?>? {
        val noteTermFrequencies = noteTermFrequencyDao.getNoteIdsForAllTermsExcept(wordsToIgnore)
        val noteIds: MutableSet<Long?> = noteTermFrequencies!!.stream()
            .map<Any?>(NoteTermFrequency::getNoteId)
            .collect(Collectors.toSet())
        val atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIds)
        return atomicNoteEntities
    }

    fun getNotesWithWordsAndFilter(
        wordsToFind: MutableSet<String?>?,
        wordsToIgnore: MutableSet<String?>,
        noteTermFrequencyDao: NoteTermFrequencyDao,
        atomicNotesDomain: AtomicNotesDomain,
        bookId: Optional<Long?>?
    ): MutableList<AtomicNoteEntity?>? {
        val noteTermFrequencies = noteTermFrequencyDao.getNoteIdsForTerms(wordsToFind)
        val noteIds: MutableSet<Long?> = noteTermFrequencies!!.stream()
            .filter { tf: NoteTermFrequency? -> !wordsToIgnore.contains(tf!!.term) }
            .map<Any?>(NoteTermFrequency::getNoteId)
            .collect(Collectors.toSet())
        val atomicNoteEntities = atomicNotesDomain.getAtomicNotes(noteIds)

        return atomicNoteEntities
    }
}
