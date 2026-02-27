package com.originb.inkwisenote2.common

import androidx.room.Database
import androidx.room.RoomDatabase
import com.originb.inkwisenote2.common.chronicle.ChronicleNotebookEntity
import com.originb.inkwisenote2.common.chronicle.ChronicleNotebooksDao
import com.originb.inkwisenote2.common.chronicle.ChronicleNoteEntity
import com.originb.inkwisenote2.common.chronicle.ChronicleNotesDao
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteEntity
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNotesDao
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import com.originb.inkwisenote2.modules.queries.data.QueriesDao
import com.originb.inkwisenote2.modules.queries.data.QueryEntity
import com.originb.inkwisenote2.modules.smartnotes.data.*
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao

@Database(
    entities = [
        NoteRelation::class,
        NoteTermFrequency::class,
        NoteOcrText::class,
        AtomicNoteEntity::class,
        SmartBookEntity::class,
        SmartBookPage::class,
        HandwrittenNoteEntity::class,
        TextNoteEntity::class,
        QueryEntity::class,
        ChronicleNotebookEntity::class,
        ChronicleNoteEntity::class
    ],
    version = 15,
    exportSchema = false
)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun noteRelationDao(): NoteRelationDao?
    abstract fun noteTermFrequencyDao(): NoteTermFrequencyDao?
    abstract fun noteOcrTextDao(): NoteOcrTextsDao?
    abstract fun atomicNoteEntitiesDao(): AtomicNoteEntitiesDao?
    abstract fun smartBooksDao(): SmartBooksDao?
    abstract fun smartBookPagesDao(): SmartBookPagesDao?
    abstract fun handwrittenNotesDao(): HandwrittenNotesDao?
    abstract fun textNotesDao(): TextNotesDao?
    abstract fun queryDao(): QueriesDao?
    abstract fun chronicleNotebooksDao(): ChronicleNotebooksDao
    abstract fun chronicleNotesDao(): ChronicleNotesDao
}
