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
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCoreDb
import org.basnalcorp.shared.systems.chroniclecore.ChronicleNotebook
import org.basnalcorp.shared.systems.chroniclecore.ChronicleNoteMeta

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
abstract class NotesDatabase : RoomDatabase(), ChronicleCoreDb {

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

    override fun insertNotebook(notebookId: String, displayName: String, creationTime: Long) {
        chronicleNotebooksDao().insert(
            ChronicleNotebookEntity(notebookId, displayName, creationTime)
        )
    }

    override fun getNotebook(notebookId: String): ChronicleNotebook? {
        val e = chronicleNotebooksDao().get(notebookId) ?: return null
        return ChronicleNotebook(
            notebookId = e.notebookId,
            displayName = e.displayName,
            creationTime = e.creationTime
        )
    }

    override fun listNotebooks(): List<ChronicleNotebook> {
        return chronicleNotebooksDao().listAll().map { e ->
            ChronicleNotebook(
                notebookId = e.notebookId,
                displayName = e.displayName,
                creationTime = e.creationTime
            )
        }
    }

    override fun updateNotebookDisplayName(notebookId: String, displayName: String) {
        chronicleNotebooksDao().updateDisplayName(notebookId, displayName)
    }

    override fun deleteNotebook(notebookId: String) {
        chronicleNotebooksDao().delete(notebookId)
    }

    override fun insertNote(
        noteId: Long,
        notebookId: String,
        title: String,
        creationTime: Long,
        lastModified: Long,
        filePath: String
    ) {
        chronicleNotesDao().insert(
            ChronicleNoteEntity(noteId, notebookId, title, creationTime, lastModified, filePath)
        )
    }

    override fun getNote(noteId: Long): ChronicleNoteMeta? {
        val e = chronicleNotesDao().get(noteId) ?: return null
        return ChronicleNoteMeta(
            noteId = e.noteId,
            notebookId = e.notebookId,
            title = e.title,
            creationTime = e.creationTime,
            lastModified = e.lastModified,
            filePath = e.filePath
        )
    }

    override fun listNotes(notebookId: String): List<ChronicleNoteMeta> {
        return chronicleNotesDao().listForNotebook(notebookId).map { e ->
            ChronicleNoteMeta(
                noteId = e.noteId,
                notebookId = e.notebookId,
                title = e.title,
                creationTime = e.creationTime,
                lastModified = e.lastModified,
                filePath = e.filePath
            )
        }
    }

    override fun updateNote(noteId: Long, title: String, lastModified: Long, filePath: String) {
        chronicleNotesDao().updateFields(noteId, title, lastModified, filePath)
    }

    override fun deleteNote(noteId: Long) {
        chronicleNotesDao().delete(noteId)
    }

    override fun deleteNotesForNotebook(notebookId: String) {
        chronicleNotesDao().deleteForNotebook(notebookId)
    }
}
