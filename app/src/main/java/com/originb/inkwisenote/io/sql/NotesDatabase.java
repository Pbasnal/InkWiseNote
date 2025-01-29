package com.originb.inkwisenote.io.sql;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.originb.inkwisenote.data.dao.NoteRelationDao;
import com.originb.inkwisenote.data.notedata.NoteRelation;

@Database(entities = {NoteRelation.class}, version = 2)
public abstract class NotesDatabase extends RoomDatabase {
    public abstract NoteRelationDao noteRelationDao();
}