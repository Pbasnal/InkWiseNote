package com.originb.inkwisenote.common;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.originb.inkwisenote.modules.handwrittennotes.data.HandwrittenNotesDao;
import com.originb.inkwisenote.modules.smartnotes.data.AtomicNoteEntitiesDao;
import com.originb.inkwisenote.modules.smartnotes.data.SmartBookPagesDao;
import com.originb.inkwisenote.modules.smartnotes.data.SmartBooksDao;
import com.originb.inkwisenote.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote.modules.ocr.data.NoteTermFrequencyDao;
import com.originb.inkwisenote.modules.noterelation.data.NoteRelationDao;
import com.originb.inkwisenote.modules.handwrittennotes.data.HandwrittenNoteEntity;
import com.originb.inkwisenote.modules.ocr.data.NoteOcrText;
import com.originb.inkwisenote.modules.ocr.data.NoteTermFrequency;
import com.originb.inkwisenote.modules.noterelation.data.NoteRelation;
import com.originb.inkwisenote.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote.modules.smartnotes.data.SmartBookEntity;
import com.originb.inkwisenote.modules.smartnotes.data.SmartBookPage;

@Database(entities = {
        NoteRelation.class,
        NoteTermFrequency.class,
        NoteOcrText.class,
        AtomicNoteEntity.class,
        SmartBookEntity.class,
        SmartBookPage.class,
        HandwrittenNoteEntity.class
}, version = 8)
public abstract class NotesDatabase extends RoomDatabase {

    public abstract NoteRelationDao noteRelationDao();

    public abstract NoteTermFrequencyDao noteTermFrequencyDao();

    public abstract NoteOcrTextDao noteOcrTextDao();

    public abstract AtomicNoteEntitiesDao atomicNoteEntitiesDao();

    public abstract SmartBooksDao smartBooksDao();

    public abstract SmartBookPagesDao smartBookPagesDao();

    public abstract HandwrittenNotesDao handwrittenNotesDao();
}