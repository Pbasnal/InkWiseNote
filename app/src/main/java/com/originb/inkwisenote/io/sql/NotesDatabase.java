package com.originb.inkwisenote.io.sql;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.originb.inkwisenote.data.dao.NoteOcrTextDao;
import com.originb.inkwisenote.data.dao.NoteRelationDao;
import com.originb.inkwisenote.data.dao.NoteTaskStatusDao;
import com.originb.inkwisenote.data.dao.NoteTermFrequencyDao;
import com.originb.inkwisenote.data.entities.notedata.NoteOcrText;
import com.originb.inkwisenote.data.entities.notedata.NoteRelation;
import com.originb.inkwisenote.data.entities.notedata.NoteTermFrequency;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStatus;

@Database(entities = {
        NoteRelation.class,
        NoteTermFrequency.class,
        NoteOcrText.class,
        NoteTaskStatus.class
}, version = 3)
@TypeConverters({
        TypeConvertersForDb.class
})
public abstract class NotesDatabase extends RoomDatabase {

    public abstract NoteRelationDao noteRelationDao();

    public abstract NoteTermFrequencyDao noteTermFrequencyDao();

    public abstract NoteOcrTextDao noteOcrTextDao();

    public abstract NoteTaskStatusDao noteTaskStatusDao();
}