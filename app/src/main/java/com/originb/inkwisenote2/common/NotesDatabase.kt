package com.originb.inkwisenote2.common

import androidx.room.Database
import androidx.room.RoomDatabase
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteEntity
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNotesDao
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import com.originb.inkwisenote2.modules.smartnotes.data.*
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao

@Database(
    entities = [NoteRelation::class, NoteTermFrequency::class, NoteOcrText::class, AtomicNoteEntity::class, SmartBookEntity::class, SmartBookPage::class, HandwrittenNoteEntity::class, TextNoteEntity::class
    ], version = 9
)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteRelationDao(): NoteRelationDao?

    abstract fun noteTermFrequencyDao(): NoteTermFrequencyDao?

    abstract fun noteOcrTextDao(): NoteOcrTextDao?

    abstract fun atomicNoteEntitiesDao(): AtomicNoteEntitiesDao?

    abstract fun smartBooksDao(): SmartBooksDao?

    abstract fun smartBookPagesDao(): SmartBookPagesDao?

    abstract fun handwrittenNotesDao(): HandwrittenNotesDao?

    abstract fun textNotesDao(): TextNotesDao?
}