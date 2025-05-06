package com.originb.inkwisenote2.common;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNotesDao;
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntitiesDao;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPagesDao;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBooksDao;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao;
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteEntity;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText;
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency;
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPage;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;
import com.originb.inkwisenote2.modules.queries.data.QueryDao;

@Database(entities = {
        NoteRelation.class,
        NoteTermFrequency.class,
        NoteOcrText.class,
        AtomicNoteEntity.class,
        SmartBookEntity.class,
        SmartBookPage.class,
        HandwrittenNoteEntity.class,
        TextNoteEntity.class,
        QueryEntity.class
}, version = 13)
public abstract class NotesDatabase extends RoomDatabase {

    public abstract NoteRelationDao noteRelationDao();

    public abstract NoteTermFrequencyDao noteTermFrequencyDao();

    public abstract NoteOcrTextDao noteOcrTextDao();

    public abstract AtomicNoteEntitiesDao atomicNoteEntitiesDao();

    public abstract SmartBooksDao smartBooksDao();

    public abstract SmartBookPagesDao smartBookPagesDao();

    public abstract HandwrittenNotesDao handwrittenNotesDao();

    public abstract TextNotesDao textNotesDao();

    public abstract QueryDao queryDao();
}