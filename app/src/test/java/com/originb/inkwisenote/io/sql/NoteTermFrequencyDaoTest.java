package com.originb.inkwisenote.io.sql;

import androidx.room.Room;
import com.originb.inkwisenote.common.NotesDatabase;
import com.originb.inkwisenote.modules.ocr.data.NoteTermFrequencyDao;
import com.originb.inkwisenote.modules.ocr.data.NoteTermFrequency;
import com.originb.inkwisenote.modules.ocr.data.TermOccurrence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class NoteTermFrequencyDaoTest {
    private NoteTermFrequencyDao noteTermFrequencyDao;
    private NotesDatabase db;

    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(),
                        NotesDatabase.class)
                .allowMainThreadQueries() // Use cautiously, only for tests
                .build();
        noteTermFrequencyDao = db.noteTermFrequencyDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testInsertAndReadTermFrequencies() {
        // Prepare test data
        Long noteId = 1L;
        List<NoteTermFrequency> noteTermFrequencyList = new ArrayList<>();
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "apple", 2));
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "banana", 1));
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "orange", 3));

        // Insert data
        noteTermFrequencyDao.insertTermFrequenciesToDb(noteTermFrequencyList);

        List<NoteTermFrequency> termFrequencies = noteTermFrequencyDao.getAllTermFrequencies();
        assertEquals(noteTermFrequencyList.size(), termFrequencies.size());

        for (NoteTermFrequency expected : noteTermFrequencyList) {
            boolean areTheyEqual = termFrequencies.stream().anyMatch(actual ->
                    actual.getNoteId() == expected.getNoteId()
                            && actual.getTerm().equals(expected.getTerm())
                            && actual.getTermFrequency() == expected.getTermFrequency()
            );
            assertTrue(areTheyEqual);
        }
    }

    @Test
    public void testGetTermOccurrences() {
        // Insert test data for multiple notes
        Long noteId = 1L;
        List<NoteTermFrequency> noteTermFrequencyList = new ArrayList<>();
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "apple", 2));
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "banana", 1));
        noteTermFrequencyDao.insertTermFrequenciesToDb(noteTermFrequencyList);

        noteId = 2L;
        noteTermFrequencyList = new ArrayList<>();
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "apple", 1));
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "orange", 3));
        noteTermFrequencyDao.insertTermFrequenciesToDb(noteTermFrequencyList);

        // Test getTermOccurrences
        Set<String> termsToQuery = new HashSet<>();
        termsToQuery.add("apple");
        termsToQuery.add("banana");
        termsToQuery.add("orange");

        List<TermOccurrence> occurrences = noteTermFrequencyDao.getTermOccurrences(termsToQuery);

        assertEquals(occurrences.size(), 3);

        assertTrue(occurrences.stream().anyMatch(to -> "apple".equals(to.getTerm()) && to.getOccurrenceCount() == 2));
        assertTrue(occurrences.stream().anyMatch(to -> "banana".equals(to.getTerm()) && to.getOccurrenceCount() == 1));
        assertTrue(occurrences.stream().anyMatch(to -> "orange".equals(to.getTerm()) && to.getOccurrenceCount() == 1));
    }

    @Test
    public void testGetDistinctNoteIdCount() {
        // Insert test data
        Long noteId = 1L;
        List<NoteTermFrequency> noteTermFrequencyList = new ArrayList<>();
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "apple", 1));
        noteTermFrequencyDao.insertTermFrequenciesToDb(noteTermFrequencyList);

        noteId = 2L;
        noteTermFrequencyList = new ArrayList<>();
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "banana", 1));
        noteTermFrequencyDao.insertTermFrequenciesToDb(noteTermFrequencyList);

        noteId = 2L;
        noteTermFrequencyList = new ArrayList<>();
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "orange", 1));
        noteTermFrequencyDao.insertTermFrequenciesToDb(noteTermFrequencyList);

        assertEquals(2, noteTermFrequencyDao.getDistinctNoteIdCount());
    }

    @Test
    public void testDeleteTermFrequencies() {
        // Insert test data
        Long noteId = 1L;
        List<NoteTermFrequency> noteTermFrequencyList = new ArrayList<>();
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "apple", 2));
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "banana", 1));
        noteTermFrequencyDao.insertTermFrequenciesToDb(noteTermFrequencyList);

        // Verify data was inserted
        assertEquals(2, noteTermFrequencyDao.readTermFrequenciesOfNote(noteId).size());

        // Delete data
        noteTermFrequencyDao.deleteTermFrequencies(noteId);

        // Verify data was deleted
        assertTrue(noteTermFrequencyDao.readTermFrequenciesOfNote(noteId).isEmpty());
    }

    @Test
    public void testGetNoteIdsForTerms() {
        // Insert test data
        Long noteId = 1L;
        List<NoteTermFrequency> noteTermFrequencyList = new ArrayList<>();
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "apple", 2));
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "banana", 1));
        noteTermFrequencyDao.insertTermFrequenciesToDb(noteTermFrequencyList);

        noteId = 2L;
        noteTermFrequencyList = new ArrayList<>();
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "apple", 1));
        noteTermFrequencyList.add(new NoteTermFrequency(noteId, "orange", 3));
        noteTermFrequencyDao.insertTermFrequenciesToDb(noteTermFrequencyList);

        // Test getNoteIdsForTerms
        Set<String> termsToQuery = new HashSet<>();
        termsToQuery.add("apple");
        termsToQuery.add("banana");

        List<NoteTermFrequency> result = noteTermFrequencyDao.getNoteIdsForTerms(termsToQuery);

        assertTrue(result.stream().anyMatch(to -> "apple".equals(to.getTerm()) && to.getNoteId() == 1));
        assertTrue(result.stream().anyMatch(to -> "apple".equals(to.getTerm()) && to.getNoteId() == 2));
        assertTrue(result.stream().anyMatch(to -> "banana".equals(to.getTerm()) && to.getNoteId() == 1));
    }
}