package com.originb.inkwisenote.io.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.originb.inkwisenote.data.notedata.NoteOcrText;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class NoteTextContractTest {
    private NoteTextContract.NoteTextDbHelper dbHelper;
    private Context context;
    private File dbFile;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        dbFile = new File(context.getCacheDir(), "test_database.db");
        dbHelper = new NoteTextContract.NoteTextDbHelper(context, dbFile.getAbsolutePath());
    }

    @After
    public void tearDown() {
        dbHelper.close();
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    public void testDatabaseCreation() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        assertTrue(db.isOpen());
    }

    @Test
    public void testInsertAndReadText() {
        // Create test data
        NoteOcrText testNote = new NoteOcrText(1L, "Test extracted text");
        
        // Insert test data
        dbHelper.insertTextToDb(testNote);
        
        // Read and verify
        List<NoteOcrText> result = dbHelper.readTextFromDb(1L);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testNote.getNoteId(), result.get(0).getNoteId());
        assertEquals(testNote.getExtractedText(), result.get(0).getExtractedText());
    }

    @Test
    public void testUpdateText() {
        // Insert initial data
        NoteOcrText initialNote = new NoteOcrText(1L, "Initial text");
        dbHelper.insertTextToDb(initialNote);
        
        // Update the text
        NoteOcrText updatedNote = new NoteOcrText(1L, "Updated text");
        dbHelper.updateTextToDb(updatedNote);
        
        // Verify update
        List<NoteOcrText> result = dbHelper.readTextFromDb(1L);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Updated text", result.get(0).getExtractedText());
    }

    @Test
    public void testSearchText() {
        // Insert test data
        dbHelper.insertTextToDb(new NoteOcrText(1L, "First test note"));
        dbHelper.insertTextToDb(new NoteOcrText(2L, "Second test note"));
        dbHelper.insertTextToDb(new NoteOcrText(3L, "Different content"));
        
        // Search for notes containing "test"
        List<Long> searchResults = dbHelper.searchTextFromDb("test");
        
        assertEquals(2, searchResults.size());
        assertTrue(searchResults.contains(1L));
        assertTrue(searchResults.contains(2L));
    }

    @Test
    public void testDeleteText() {
        // Insert test data
        NoteOcrText testNote = new NoteOcrText(1L, "Test note to delete");
        dbHelper.insertTextToDb(testNote);
        
        // Verify insertion
        List<NoteOcrText> beforeDelete = dbHelper.readTextFromDb(1L);
        assertEquals(1, beforeDelete.size());
        
        // Delete the note
        dbHelper.deleteNoteText(1L);
        
        // Verify deletion
        List<NoteOcrText> afterDelete = dbHelper.readTextFromDb(1L);
        assertTrue(afterDelete.isEmpty());
    }

    @Test
    public void testDatabaseUpgrade() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onUpgrade(db, 1, 2);
        assertTrue(db.isOpen());
    }
} 