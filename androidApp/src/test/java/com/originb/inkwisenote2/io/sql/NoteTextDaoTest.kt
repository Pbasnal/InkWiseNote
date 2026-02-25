package com.originb.inkwisenote2.io.sql

import androidx.room.Room.inMemoryDatabaseBuilder
import com.originb.inkwisenote2.common.NotesDatabase
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.stream.Collectors

@RunWith(RobolectricTestRunner::class)
class NoteTextDaoTest {
    private var noteOcrTextDao: NoteOcrTextsDao? = null

    private var db: NotesDatabase? = null

    @Before
    fun setUp() {
        db = inMemoryDatabaseBuilder<NotesDatabase>(RuntimeEnvironment.getApplication(), NotesDatabase::class.java)
            .allowMainThreadQueries() // Use cautiously, only for tests
            .build()
        noteOcrTextDao = db!!.noteOcrTextDao()
    }

    @After
    fun tearDown() {
        db!!.close()
    }

    @Test
    fun testInsertAndReadText() {
        // Create test data
        val testNote = NoteOcrText(1L, "", "Test extracted text")

        // Insert test data
        noteOcrTextDao!!.insertTextToDb(testNote)

        // Read and verify
        val result = noteOcrTextDao!!.readTextFromDb(1L)

        Assert.assertFalse(result == null)
        Assert.assertEquals(testNote.noteId, result.noteId)
        Assert.assertEquals(testNote.extractedText, result.extractedText)
    }

    @Test
    fun testUpdateText() {
        // Insert initial data
        val initialNote = NoteOcrText(1L, "", "Initial text")
        noteOcrTextDao!!.insertTextToDb(initialNote)

        // Update the text
        val updatedNote = NoteOcrText(1L, "", "Updated text")
        noteOcrTextDao!!.updateTextToDb(updatedNote)

        // Verify update
        val result = noteOcrTextDao!!.readTextFromDb(1L)

        Assert.assertFalse(result == null)
        Assert.assertEquals("Updated text", result.extractedText)
    }

    @Test
    fun testSearchText() {
        // Insert test data
        noteOcrTextDao!!.insertTextToDb(NoteOcrText(1L, "", "First test note"))
        noteOcrTextDao!!.insertTextToDb(NoteOcrText(2L, "", "Second test note"))
        noteOcrTextDao!!.insertTextToDb(NoteOcrText(3L, "", "Different content"))

        // Search for notes containing "test"
        val searchResults = noteOcrTextDao!!.searchTextFromDb("test")
            .stream().map<Long?>(NoteOcrText::noteId).collect(Collectors.toList())

        Assert.assertEquals(2, searchResults.size.toLong())

        Assert.assertTrue(searchResults.contains(1L))
        Assert.assertTrue(searchResults.contains(2L))
    }

    @Test
    fun testDeleteText() {
        // Insert test data
        val testNote = NoteOcrText(1L, "", "Test note to delete")
        noteOcrTextDao!!.insertTextToDb(testNote)

        // Verify insertion
        val beforeDelete = noteOcrTextDao!!.readTextFromDb(1L)
        Assert.assertTrue(beforeDelete != null)

        // Delete the note
        noteOcrTextDao!!.deleteNoteText(1L)

        // Verify deletion
        val afterDelete = noteOcrTextDao!!.readTextFromDb(1L)
        Assert.assertTrue(afterDelete == null)
    }
}