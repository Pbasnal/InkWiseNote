package com.originb.inkwisenote2.modules.tfidf

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import com.originb.inkwisenote2.common.NotesDatabase
import com.originb.inkwisenote2.modules.noterelation.service.NoteTfIdfLogic
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class NoteTfIdfLogicTest {
    private var noteTfIdfLogic: NoteTfIdfLogic? = null
    private var noteTermFrequencyDao: NoteTermFrequencyDao? = null
    private var db: NotesDatabase? = null
    private var dbFile: File? = null

    @Before
    fun setUp() {
        val context: Context = RuntimeEnvironment.application
        val directory = File(System.getProperty("user.dir"))
        val testDbPath = directory.toString() + "/src/test/resources/test_database.db"
        dbFile = File(testDbPath)
        // Delete the database file if it exists
        if (dbFile!!.exists()) {
            dbFile!!.delete()
        }

        db = inMemoryDatabaseBuilder<NotesDatabase>(context, NotesDatabase::class.java)
            .allowMainThreadQueries() // Use cautiously, only for tests
            .build()
        noteTermFrequencyDao = db!!.noteTermFrequencyDao()

        noteTfIdfLogic = NoteTfIdfLogic(noteTermFrequencyDao!!)
    }

    @After
    fun tearDown() {
        db!!.close()
        //        noteTermFrequencyDao.dropTable();
        if (dbFile!!.exists()) {
            dbFile!!.delete()
        }
    }

    @Test
    fun testAddNewNote() {
        // Add a new note
        val noteId = 1L
        val terms = mutableListOf<String>("apple", "banana", "apple", "orange")
        noteTfIdfLogic!!.addOrUpdateNote(noteId, terms)

        // Verify term frequencies
        val termFrequencies = toTermFrequencyMap(noteTermFrequencyDao!!.readTermFrequenciesOfNote(noteId))

        Assert.assertEquals(2, (termFrequencies.get("apple") as Int).toLong())
        Assert.assertEquals(1, (termFrequencies.get("banana") as Int).toLong())
        Assert.assertEquals(1, (termFrequencies.get("orange") as Int).toLong())

        // Verify IDF scores (with only one document, IDF = ln(1/1) = 0)
        val tfIdfScores: MutableMap<String, Double> = noteTfIdfLogic!!.getTfIdf(noteId)
        Assert.assertEquals(0.0, tfIdfScores.get("apple")!!, 0.001)
        Assert.assertEquals(0.0, tfIdfScores.get("banana")!!, 0.001)
        Assert.assertEquals(0.0, tfIdfScores.get("orange")!!, 0.001)
    }

    //    @Test
    //    public void testUpdateExistingNote() {
    //        // Add initial note
    //        Long noteId = 1L;
    //        List<String> initialTerms = Arrays.asList("apple", "banana");
    //        noteTfIdfLogic.addOrUpdateNote(noteId, initialTerms);
    //
    //        // Update the note
    //        List<String> updatedTerms = Arrays.asList("apple", "orange");
    //        noteTfIdfLogic.addOrUpdateNote(noteId, updatedTerms);
    //
    //        // Verify updated term frequencies
    //        Map<String, Integer> termFrequencies = noteTermFrequencyDbQueries.readTermFrequenciesOfNote(noteId);
    //        assertEquals(1, (int) termFrequencies.get("apple"));
    //        assertNull(termFrequencies.get("banana")); // Should be removed
    //        assertEquals(1, (int) termFrequencies.get("orange")); // Should be added
    //    }
    //
    //    @Test
    //    public void testDeleteNote() {
    //        // Add a note
    //        Long noteId = 1L;
    //        List<String> terms = Arrays.asList("apple", "banana");
    //        noteTfIdfLogic.addOrUpdateNote(noteId, terms);
    //
    //        // Delete the note
    //        noteTfIdfLogic.deleteDocument(noteId);
    //
    //        // Verify note is deleted
    //        Map<String, Integer> termFrequencies = noteTermFrequencyDbQueries.readTermFrequenciesOfNote(noteId);
    //        assertTrue(termFrequencies.isEmpty());
    //    }
    //
    //    @Test
    //    public void testGetRelatedDocuments() {
    //        // Add two notes with some overlapping terms
    //        Long noteId1 = 1L;
    //        List<String> terms1 = Arrays.asList("apple", "banana");
    //        noteTfIdfLogic.addOrUpdateNote(noteId1, terms1);
    //
    //        Long noteId2 = 2L;
    //        List<String> terms2 = Arrays.asList("apple", "orange");
    //        noteTfIdfLogic.addOrUpdateNote(noteId2, terms2);
    //
    //        // Query for related documents
    //        Set<String> queryTerms = new HashSet<>(Arrays.asList("apple", "banana"));
    //        Map<String, Set<Long>> relatedDocs = noteTfIdfLogic.getRelatedDocuments(queryTerms);
    //
    //        // Verify results
    //        assertTrue(relatedDocs.get("apple").contains(noteId1));
    //        assertTrue(relatedDocs.get("apple").contains(noteId2));
    //        assertTrue(relatedDocs.get("banana").contains(noteId1));
    //        assertFalse(relatedDocs.containsKey("orange"));
    //    }
    //
    //    @Test
    //    public void testTfIdfCalculation() {
    //        // Add multiple documents to test TF-IDF calculation
    //        Long noteId1 = 1L;
    //        List<String> terms1 = Arrays.asList("apple", "banana", "apple"); // apple appears twice
    //        noteTfIdfLogic.addOrUpdateNote(noteId1, terms1);
    //
    //        Long noteId2 = 2L;
    //        List<String> terms2 = Arrays.asList("apple", "orange");
    //        noteTfIdfLogic.addOrUpdateNote(noteId2, terms2);
    //
    //        // Calculate expected TF-IDF values
    //        // For noteId1:
    //        // TF(apple) = 2/3, IDF(apple) = ln(2/2) = 0
    //        // TF(banana) = 1/3, IDF(banana) = ln(2/1) = 0.693
    //
    //        Map<String, Double> tfIdfScores1 = noteTfIdfLogic.getTfIdf(noteId1);
    //        assertEquals(0.0, tfIdfScores1.get("apple"), 0.001); // TF * IDF = 2/3 * 0 = 0
    //        assertEquals(0.231, tfIdfScores1.get("banana"), 0.001); // TF * IDF = 1/3 * 0.693 = 0.231
    //    }
    private fun toTermFrequencyMap(noteTermFrequencies: MutableList<NoteTermFrequency>): MutableMap<String?, Int?> {
        val termFrequenciesOfNote: MutableMap<String?, Int?> = HashMap<String?, Int?>()
        for (noteTermFrequency in noteTermFrequencies) {
            termFrequenciesOfNote.put(noteTermFrequency.term, noteTermFrequency.termFrequency)
        }
        return termFrequenciesOfNote
    }
}