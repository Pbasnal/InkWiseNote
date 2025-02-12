package com.originb.inkwisenote.io.sql;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.originb.inkwisenote.data.dao.*;
import com.originb.inkwisenote.data.entities.notedata.*;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStatus;

@Database(entities = {
        NoteRelation.class,
        NoteTermFrequency.class,
        NoteOcrText.class,
        NoteTaskStatus.class,
        AtomicNoteEntity.class,
        SmartBookEntity.class,
        SmartBookPage.class
}, version = 6)
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
}