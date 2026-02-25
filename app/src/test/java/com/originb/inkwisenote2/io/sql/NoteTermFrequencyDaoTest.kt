package com.originb.inkwisenote2.io.sql

import androidx.room.Room.inMemoryDatabaseBuilder
import com.originb.inkwisenote2.common.NotesDatabase
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import com.originb.inkwisenote2.modules.ocr.data.TermOccurrence
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class NoteTermFrequencyDaoTest {
    private var noteTermFrequencyDao: NoteTermFrequencyDao? = null
    private var db: NotesDatabase? = null

    @Before
    fun setUp() {
        db = inMemoryDatabaseBuilder<NotesDatabase>(
            RuntimeEnvironment.getApplication(),
            NotesDatabase::class.java
        )
            .allowMainThreadQueries() // Use cautiously, only for tests
            .build()
        noteTermFrequencyDao = db!!.noteTermFrequencyDao()
    }

    @After
    fun tearDown() {
        db!!.close()
    }

    @Test
    fun testInsertAndReadTermFrequencies() {
        // Prepare test data
        val noteId = 1L
        val noteTermFrequencyList: MutableList<NoteTermFrequency> = ArrayList<NoteTermFrequency>()
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "apple", 2))
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "banana", 1))
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "orange", 3))

        // Insert data
        noteTermFrequencyDao!!.insertTermFrequenciesToDb(noteTermFrequencyList)

        val termFrequencies: MutableList<NoteTermFrequency> = noteTermFrequencyDao!!.allTermFrequencies
        Assert.assertEquals(noteTermFrequencyList.size.toLong(), termFrequencies.size.toLong())

        for (expected in noteTermFrequencyList) {
            val areTheyEqual = termFrequencies.stream().anyMatch { actual: NoteTermFrequency? ->
                actual!!.noteId == expected.noteId && actual.term == expected.term
                        && actual.termFrequency == expected.termFrequency
            }
            Assert.assertTrue(areTheyEqual)
        }
    }

    @Test
    fun testGetTermOccurrences() {
        // Insert test data for multiple notes
        var noteId = 1L
        var noteTermFrequencyList: MutableList<NoteTermFrequency> = ArrayList<NoteTermFrequency>()
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "apple", 2))
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "banana", 1))
        noteTermFrequencyDao!!.insertTermFrequenciesToDb(noteTermFrequencyList)

        noteId = 2L
        noteTermFrequencyList = ArrayList<NoteTermFrequency>()
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "apple", 1))
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "orange", 3))
        noteTermFrequencyDao!!.insertTermFrequenciesToDb(noteTermFrequencyList)

        // Test getTermOccurrences
        val termsToQuery: MutableSet<String> = HashSet<String>()
        termsToQuery.add("apple")
        termsToQuery.add("banana")
        termsToQuery.add("orange")

        val occurrences: MutableList<TermOccurrence> = noteTermFrequencyDao!!.getTermOccurrences(termsToQuery)

        Assert.assertEquals(occurrences.size.toLong(), 3)

        Assert.assertTrue(
            occurrences.stream().anyMatch { to: TermOccurrence? -> "apple" == to!!.term && to.occurrenceCount == 2 })
        Assert.assertTrue(
            occurrences.stream().anyMatch { to: TermOccurrence? -> "banana" == to!!.term && to.occurrenceCount == 1 })
        Assert.assertTrue(
            occurrences.stream().anyMatch { to: TermOccurrence? -> "orange" == to!!.term && to.occurrenceCount == 1 })
    }

    @Test
    fun testGetDistinctNoteIdCount() {
        // Insert test data
        var noteId = 1L
        var noteTermFrequencyList: MutableList<NoteTermFrequency> = ArrayList<NoteTermFrequency>()
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "apple", 1))
        noteTermFrequencyDao!!.insertTermFrequenciesToDb(noteTermFrequencyList)

        noteId = 2L
        noteTermFrequencyList = ArrayList<NoteTermFrequency>()
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "banana", 1))
        noteTermFrequencyDao!!.insertTermFrequenciesToDb(noteTermFrequencyList)

        noteId = 2L
        noteTermFrequencyList = ArrayList<NoteTermFrequency>()
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "orange", 1))
        noteTermFrequencyDao!!.insertTermFrequenciesToDb(noteTermFrequencyList)

        assertEquals(2, noteTermFrequencyDao!!.distinctNoteIdCount)
    }

    @Test
    fun testDeleteTermFrequencies() {
        // Insert test data
        val noteId = 1L
        val noteTermFrequencyList: MutableList<NoteTermFrequency> = ArrayList<NoteTermFrequency>()
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "apple", 2))
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "banana", 1))
        noteTermFrequencyDao!!.insertTermFrequenciesToDb(noteTermFrequencyList)

        // Verify data was inserted
        Assert.assertEquals(2, noteTermFrequencyDao!!.readTermFrequenciesOfNote(noteId).size.toLong())

        // Delete data
        noteTermFrequencyDao!!.deleteTermFrequencies(noteId)

        // Verify data was deleted
        Assert.assertTrue(noteTermFrequencyDao!!.readTermFrequenciesOfNote(noteId).isEmpty())
    }

    @Test
    fun testGetNoteIdsForTerms() {
        // Insert test data
        var noteId = 1L
        var noteTermFrequencyList: MutableList<NoteTermFrequency> = ArrayList<NoteTermFrequency>()
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "apple", 2))
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "banana", 1))
        noteTermFrequencyDao!!.insertTermFrequenciesToDb(noteTermFrequencyList)

        noteId = 2L
        noteTermFrequencyList = ArrayList<NoteTermFrequency>()
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "apple", 1))
        noteTermFrequencyList.add(NoteTermFrequency(noteId, "orange", 3))
        noteTermFrequencyDao!!.insertTermFrequenciesToDb(noteTermFrequencyList)

        // Test getNoteIdsForTerms
        val termsToQuery: MutableSet<String> = HashSet<String>()
        termsToQuery.add("apple")
        termsToQuery.add("banana")

        val result: MutableList<NoteTermFrequency> = noteTermFrequencyDao!!.getNoteIdsForTerms(termsToQuery)

        Assert.assertTrue(
            result.stream().anyMatch { to: NoteTermFrequency? -> "apple" == to!!.term && to.noteId == 1L })
        Assert.assertTrue(
            result.stream().anyMatch { to: NoteTermFrequency? -> "apple" == to!!.term && to.noteId == 2L })
        Assert.assertTrue(
            result.stream().anyMatch { to: NoteTermFrequency? -> "banana" == to!!.term && to.noteId == 1L })
    }
}