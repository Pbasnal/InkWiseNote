package com.originb.inkwisenote.io.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.originb.inkwisenote.modules.backgroundjobs.data.TextProcessingJobStatus;
import com.originb.inkwisenote.modules.backgroundjobs.data.TextProcessingStage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class TextProcessingJobContractTest {
    private TextProcessingJobContract.TextProcessingDbQueries dbHelper;
    private Context context;
    private File dbFile;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        File directory = new File(System.getProperty("user.dir"));
        dbFile = new File(directory + "/src/test/resources/test_database.db");
        dbHelper = new TextProcessingJobContract.TextProcessingDbQueries(context, dbFile.getAbsolutePath());
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
    public void testInsertAndReadJob() {
        // Insert a job
        Long noteId = 1L;
        dbHelper.insertJob(noteId);

        // Read and verify
        TextProcessingJobStatus status = dbHelper.readFirstNoteJobStatus();

        assertNotNull(status);
        assertEquals(noteId, status.getNoteId());
        assertEquals(TextProcessingStage.TOKENIZATION, status.getStage());
    }

    @Test
    public void testDeleteJob() {
        // Insert a job
        Long noteId = 1L;
        dbHelper.insertJob(noteId);

        // Verify insertion
        TextProcessingJobStatus beforeDelete = dbHelper.readFirstNoteJobStatus();
        assertNotNull(beforeDelete);

        // Delete the job
        dbHelper.deleteJob(noteId);

        // Verify deletion
        TextProcessingJobStatus afterDelete = dbHelper.readFirstNoteJobStatus();
        assertNull(afterDelete);
    }

    @Test
    public void testMultipleJobsOrder() {
        // Insert multiple jobs
        dbHelper.insertJob(1L);
        dbHelper.insertJob(2L);
        dbHelper.insertJob(3L);

        // Verify that we get the most recent job (highest note ID)
        TextProcessingJobStatus status = dbHelper.readFirstNoteJobStatus();
        assertNotNull(status);
        assertEquals(Long.valueOf(3L), status.getNoteId());
    }

    @Test
    public void testDatabaseUpgrade() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onUpgrade(db, 1, 2);
        assertTrue(db.isOpen());
    }
} 