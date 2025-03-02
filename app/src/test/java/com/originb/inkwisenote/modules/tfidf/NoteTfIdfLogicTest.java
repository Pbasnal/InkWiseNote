package com.originb.inkwisenote.modules.tfidf;

import android.content.Context;
import androidx.room.Room;
import com.originb.inkwisenote.data.dao.noteocr.NoteTermFrequencyDao;
import com.originb.inkwisenote.data.entities.noteocrdata.NoteTermFrequency;
import com.originb.inkwisenote.io.sql.NotesDatabase;
import com.originb.inkwisenote.modules.repositories.Repositories;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class NoteTfIdfLogicTest {
    private NoteTfIdfLogic noteTfIdfLogic;
    private NoteTermFrequencyDao noteTermFrequencyDao;
    private NotesDatabase db;
    private File dbFile;

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.application;
        File directory = new File(System.getProperty("user.dir"));
        String testDbPath = directory + "/src/test/resources/test_database.db";
        dbFile = new File(testDbPath);
        // Delete the database file if it exists
        if (dbFile.exists()) {
            dbFile.delete();
        }

        Repositories.registerRepositories(context);
        Repositories repositories = Repositories.getInstance();

        db = Room.inMemoryDatabaseBuilder(context, NotesDatabase.class)
                .allowMainThreadQueries() // Use cautiously, only for tests
                .build();
        noteTermFrequencyDao = db.noteTermFrequencyDao();
        repositories.setNotesDb(db);
//        noteTermFrequencyDbQueries = new NoteTermFrequencyDbQueries(context, testDbPath);
//        SQLiteDatabase db = noteTermFrequencyDbQueries.getWritableDatabase();
//        repositories.setNoteTermFrequencyDao(noteTermFrequencyDbQueries);

        noteTfIdfLogic = new NoteTfIdfLogic(repositories);
    }

    @After
    public void tearDown() {
        db.close();
//        noteTermFrequencyDao.dropTable();
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    public void testAddNewNote() {
        // Add a new note
        Long noteId = 1L;
        List<String> terms = Arrays.asList("apple", "banana", "apple", "orange");
        noteTfIdfLogic.addOrUpdateNote(noteId, terms);

        // Verify term frequencies
        Map<String, Integer> termFrequencies = toTermFrequencyMap(noteTermFrequencyDao.readTermFrequenciesOfNote(noteId));

        assertEquals(2, (int) termFrequencies.get("apple"));
        assertEquals(1, (int) termFrequencies.get("banana"));
        assertEquals(1, (int) termFrequencies.get("orange"));

        // Verify IDF scores (with only one document, IDF = ln(1/1) = 0)
        Map<String, Double> tfIdfScores = noteTfIdfLogic.getTfIdf(noteId);
        assertEquals(0.0, tfIdfScores.get("apple"), 0.001);
        assertEquals(0.0, tfIdfScores.get("banana"), 0.001);
        assertEquals(0.0, tfIdfScores.get("orange"), 0.001);
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

    private Map<String, Integer> toTermFrequencyMap(List<NoteTermFrequency> noteTermFrequencies) {
        Map<String, Integer> termFrequenciesOfNote = new HashMap<>();
        for (NoteTermFrequency noteTermFrequency : noteTermFrequencies) {
            termFrequenciesOfNote.put(noteTermFrequency.getTerm(), noteTermFrequency.getTermFrequency());
        }
        return termFrequenciesOfNote;
    }
} 