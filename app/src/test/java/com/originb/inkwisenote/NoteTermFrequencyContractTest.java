package com.originb.inkwisenote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.originb.inkwisenote.io.sql.NoteTermFrequencyContract;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class NoteTermFrequencyContractTest {
    private NoteTermFrequencyContract.NoteTermFrequencyDbQueries dbQueries;
    private SQLiteDatabase db;
    private File dbFile;  // Add this field

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.application;
        File directory = new File(System.getProperty("user.dir"));
        String testImageFilePath = directory + "/src/test/resources/test_database.db";
        dbFile = new File(testImageFilePath);
        dbQueries = new NoteTermFrequencyContract.NoteTermFrequencyDbQueries(context, dbFile.getAbsolutePath());
        db = dbQueries.getWritableDatabase();
    }

    @After
    public void tearDown() {
        db.close();
        dbQueries.close();
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    public void testInsertAndReadTermFrequencies() {
        // Prepare test data
        Long noteId = 1L;
        Map<String, Integer> termFrequencies = new HashMap<>();
        termFrequencies.put("apple", 2);
        termFrequencies.put("banana", 1);
        termFrequencies.put("orange", 3);

        // Insert data
        dbQueries.insertTermFrequencieToDb(noteId, termFrequencies);

        // Read and verify
        Map<String, Integer> retrieved = dbQueries.readTermFrequenciesOfNote(noteId);
        assertEquals(termFrequencies, retrieved);
    }

    @Test
    public void testGetTermOccurrences() {
        // Insert test data for multiple notes
        Map<String, Integer> terms1 = new HashMap<>();
        terms1.put("apple", 2);
        terms1.put("banana", 1);
        dbQueries.insertTermFrequencieToDb(1L, terms1);

        Map<String, Integer> terms2 = new HashMap<>();
        terms2.put("apple", 1);
        terms2.put("orange", 3);
        dbQueries.insertTermFrequencieToDb(2L, terms2);

        // Test getTermOccurrences
        Set<String> termsToQuery = new HashSet<>();
        termsToQuery.add("apple");
        termsToQuery.add("banana");
        termsToQuery.add("orange");

        Map<String, Integer> occurrences = dbQueries.getTermOccurrences(termsToQuery);

        assertEquals(2, (int) occurrences.get("apple")); // appears in 2 notes
        assertEquals(1, (int) occurrences.get("banana")); // appears in 1 note
        assertEquals(1, (int) occurrences.get("orange")); // appears in 1 note
    }

    @Test
    public void testGetDistinctNoteIdCount() {
        // Insert test data
        Map<String, Integer> terms1 = new HashMap<>();
        terms1.put("apple", 1);
        dbQueries.insertTermFrequencieToDb(1L, terms1);

        Map<String, Integer> terms2 = new HashMap<>();
        terms2.put("banana", 1);
        dbQueries.insertTermFrequencieToDb(2L, terms2);

        Map<String, Integer> terms3 = new HashMap<>();
        terms3.put("orange", 1);
        dbQueries.insertTermFrequencieToDb(2L, terms3); // Same noteId as terms2

        assertEquals(2, dbQueries.getDistinctNoteIdCount());
    }

    @Test
    public void testDeleteTermFrequencies() {
        // Insert test data
        Long noteId = 1L;
        Map<String, Integer> termFrequencies = new HashMap<>();
        termFrequencies.put("apple", 2);
        termFrequencies.put("banana", 1);
        dbQueries.insertTermFrequencieToDb(noteId, termFrequencies);

        // Verify data was inserted
        assertFalse(dbQueries.readTermFrequenciesOfNote(noteId).isEmpty());

        // Delete data
        dbQueries.deleteTermFrequencies(noteId);

        // Verify data was deleted
        assertTrue(dbQueries.readTermFrequenciesOfNote(noteId).isEmpty());
    }

    @Test
    public void testGetNoteIdsForTerms() {
        // Insert test data
        Map<String, Integer> terms1 = new HashMap<>();
        terms1.put("apple", 2);
        terms1.put("banana", 1);
        dbQueries.insertTermFrequencieToDb(1L, terms1);

        Map<String, Integer> terms2 = new HashMap<>();
        terms2.put("apple", 1);
        terms2.put("orange", 3);
        dbQueries.insertTermFrequencieToDb(2L, terms2);

        // Test getNoteIdsForTerms
        Set<String> termsToQuery = new HashSet<>();
        termsToQuery.add("apple");
        termsToQuery.add("banana");

        Map<String, Set<Long>> termNoteIds = dbQueries.getNoteIdsForTerms(termsToQuery);

        // Verify results
        assertTrue(termNoteIds.get("apple").contains(1L));
        assertTrue(termNoteIds.get("apple").contains(2L));
        assertTrue(termNoteIds.get("banana").contains(1L));
        assertEquals(2, termNoteIds.get("apple").size());
        assertEquals(1, termNoteIds.get("banana").size());
    }
}