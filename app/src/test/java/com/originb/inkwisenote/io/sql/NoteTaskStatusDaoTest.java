package com.originb.inkwisenote.io.sql;

import androidx.room.Room;
import com.originb.inkwisenote.data.dao.tasks.NoteTaskStatusDao;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskName;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStatus;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStage;
import com.originb.inkwisenote.data.entities.tasks.TfIdfRelationTasks;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class NoteTaskStatusDaoTest {
    private NoteTaskStatusDao noteTaskStatusDao;
    private NotesDatabase db;
    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), NotesDatabase.class)
                .allowMainThreadQueries() // Use cautiously, only for tests
                .build();
        noteTaskStatusDao = db.noteTaskStatusDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testInsertAndReadJob() {
        // Insert a job
        long noteId = 1L;
        noteTaskStatusDao.insertNoteTask(TfIdfRelationTasks.newTask(noteId));

        // Read and verify
        NoteTaskStatus status = noteTaskStatusDao.getNoteStatus(noteId, NoteTaskName.TF_IDF_RELATION);

        assertNotNull(status);
        assertEquals(noteId, status.getNoteId());
        assertEquals(NoteTaskStage.TEXT_PARSING, status.getStage());
    }

    @Test
    public void testInsertAndReadJobForTokenization() {
        // Insert a job
        long noteId = 1L;
        noteTaskStatusDao.insertNoteTask(TfIdfRelationTasks.newTask(noteId));
        noteTaskStatusDao.updateNoteTask(TfIdfRelationTasks.tokenizationTask(noteId));

        // Read and verify
        NoteTaskStatus status = noteTaskStatusDao.getNoteStatus(noteId, NoteTaskName.TF_IDF_RELATION);

        assertNotNull(status);
        assertEquals(noteId, status.getNoteId());
        assertEquals(NoteTaskStage.TOKENIZATION, status.getStage());
    }

    @Test
    public void testDeleteJob() {
        // Insert a job
        long noteId = 1L;
        noteTaskStatusDao.insertNoteTask(TfIdfRelationTasks.newTask(noteId));

        // Verify insertion
        NoteTaskStatus beforeDelete = noteTaskStatusDao.getNoteStatus(noteId, NoteTaskName.TF_IDF_RELATION);
        assertNotNull(beforeDelete);

        // Delete the job
        noteTaskStatusDao.deleteNoteTask(noteId, NoteTaskName.TF_IDF_RELATION);

        // Verify deletion
        NoteTaskStatus afterDelete = noteTaskStatusDao.getNoteStatus(noteId, NoteTaskName.TF_IDF_RELATION);
        assertNull(afterDelete);
    }
} 