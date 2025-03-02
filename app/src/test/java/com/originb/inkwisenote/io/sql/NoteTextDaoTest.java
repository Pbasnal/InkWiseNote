package com.originb.inkwisenote.io.sql;

import androidx.room.Room;
import com.originb.inkwisenote.data.dao.noteocr.NoteOcrTextDao;
import com.originb.inkwisenote.data.entities.noteocrdata.NoteOcrText;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class NoteTextDaoTest {
    private NoteOcrTextDao noteOcrTextDao;

    private NotesDatabase db;

    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), NotesDatabase.class)
                .allowMainThreadQueries() // Use cautiously, only for tests
                .build();
        noteOcrTextDao = db.noteOcrTextDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testInsertAndReadText() {
        // Create test data
        NoteOcrText testNote = new NoteOcrText(1L, "", "Test extracted text");

        // Insert test data
        noteOcrTextDao.insertTextToDb(testNote);

        // Read and verify
        NoteOcrText result = noteOcrTextDao.readTextFromDb(1L);

        assertFalse(result == null);
        assertEquals(testNote.getNoteId(), result.getNoteId());
        assertEquals(testNote.getExtractedText(), result.getExtractedText());
    }

    @Test
    public void testUpdateText() {
        // Insert initial data
        NoteOcrText initialNote = new NoteOcrText(1L, "", "Initial text");
        noteOcrTextDao.insertTextToDb(initialNote);

        // Update the text
        NoteOcrText updatedNote = new NoteOcrText(1L, "","Updated text");
        noteOcrTextDao.updateTextToDb(updatedNote);

        // Verify update
        NoteOcrText result = noteOcrTextDao.readTextFromDb(1L);

        assertFalse(result == null);
        assertEquals("Updated text", result.getExtractedText());
    }

    @Test
    public void testSearchText() {
        // Insert test data
        noteOcrTextDao.insertTextToDb(new NoteOcrText(1L,"", "First test note"));
        noteOcrTextDao.insertTextToDb(new NoteOcrText(2L,"", "Second test note"));
        noteOcrTextDao.insertTextToDb(new NoteOcrText(3L, "","Different content"));

        // Search for notes containing "test"
        List<Long> searchResults = noteOcrTextDao.searchTextFromDb("test")
                .stream().map(NoteOcrText::getNoteId).collect(Collectors.toList());

        assertEquals(2, searchResults.size());

        assertTrue(searchResults.contains(1L));
        assertTrue(searchResults.contains(2L));
    }

    @Test
    public void testDeleteText() {
        // Insert test data
        NoteOcrText testNote = new NoteOcrText(1L, "","Test note to delete");
        noteOcrTextDao.insertTextToDb(testNote);

        // Verify insertion
        NoteOcrText beforeDelete = noteOcrTextDao.readTextFromDb(1L);
        assertTrue(beforeDelete != null);

        // Delete the note
        noteOcrTextDao.deleteNoteText(1L);

        // Verify deletion
        NoteOcrText afterDelete = noteOcrTextDao.readTextFromDb(1L);
        assertTrue(afterDelete == null);
    }
} 