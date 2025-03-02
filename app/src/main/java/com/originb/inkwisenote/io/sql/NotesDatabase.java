package com.originb.inkwisenote.io.sql;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.originb.inkwisenote.data.dao.handwrittennotes.HandwrittenNotesDao;
import com.originb.inkwisenote.data.dao.notes.AtomicNoteEntitiesDao;
import com.originb.inkwisenote.data.dao.notes.SmartBookPagesDao;
import com.originb.inkwisenote.data.dao.notes.SmartBooksDao;
import com.originb.inkwisenote.data.dao.noteocr.NoteOcrTextDao;
import com.originb.inkwisenote.data.dao.noteocr.NoteTermFrequencyDao;
import com.originb.inkwisenote.data.dao.noterelation.NoteRelationDao;
import com.originb.inkwisenote.data.dao.tasks.NoteTaskStatusDao;
import com.originb.inkwisenote.data.entities.handwrittennotedata.HandwrittenNoteEntity;
import com.originb.inkwisenote.data.entities.notedata.*;
import com.originb.inkwisenote.data.entities.noteocrdata.NoteOcrText;
import com.originb.inkwisenote.data.entities.noteocrdata.NoteTermFrequency;
import com.originb.inkwisenote.data.entities.noterelationdata.NoteRelation;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStatus;

@Database(entities = {
        NoteRelation.class,
        NoteTermFrequency.class,
        NoteOcrText.class,
        NoteTaskStatus.class,
        AtomicNoteEntity.class,
        SmartBookEntity.class,
        SmartBookPage.class,
        HandwrittenNoteEntity.class
}, version = 7)
@TypeConverters({
        TypeConvertersForDb.class
})
public abstract class NotesDatabase extends RoomDatabase {

    public abstract NoteRelationDao noteRelationDao();

    public abstract NoteTermFrequencyDao noteTermFrequencyDao();

    public abstract NoteOcrTextDao noteOcrTextDao();

    public abstract NoteTaskStatusDao noteTaskStatusDao();

    public abstract AtomicNoteEntitiesDao atomicNoteEntitiesDao();

    public abstract SmartBooksDao smartBooksDao();

    public abstract SmartBookPagesDao smartBookPagesDao();

    public abstract HandwrittenNotesDao handwrittenNotesDao();
}