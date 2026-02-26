package com.originb.inkwisenote2.modules.smarthome.utils

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import com.originb.inkwisenote2.modules.backgroundjobs.Events.TextNoteSaved
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.handwrittennotes.data.Stroke
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import org.koin.java.KoinJavaComponent.get
import java.util.UUID

/**
 * Programmatic seed data utilities for Smart Home instrumented tests.
 * Uses the same repositories and EventBus as the app so the full pipeline
 * (SmartNotebookSaved, TextNoteSaved, HandwrittenNoteSaved) runs.
 */
object SmartHomeSeedDataUtils {

    /** Prefix for test-created notebook titles so teardown can find them. */
    const val TEST_SEED_TITLE_PREFIX = "test_seed_"

    /**
     * Creates a new notebook with one NOT_SET atomic note, persists it, and posts SmartNotebookSaved.
     * Uses a unique title so tests can clean up by prefix.
     *
     * @param context Application context (e.g. ApplicationProvider.getApplicationContext())
     * @param workingPath Directory for notebooks (e.g. context.filesDir.path)
     * @param titlePrefix Optional prefix; if null uses [TEST_SEED_TITLE_PREFIX] + UUID
     * @return The created [SmartNotebook] (already in DB; event posted)
     */
    fun createNotebook(
        context: Context,
        workingPath: String,
        titlePrefix: String? = null
    ): SmartNotebook {
        val repository: SmartNotebookRepository = get(SmartNotebookRepository::class.java)
        val title = (titlePrefix ?: TEST_SEED_TITLE_PREFIX) + UUID.randomUUID().toString()
        val notebook = repository.initializeNewSmartNotebook(title, workingPath, NoteType.NOT_SET)
        val app = context.applicationContext as? Application
        if (app != null) repository.updateNotebook(notebook, app)
        return notebook
    }

    /**
     * Converts the first note to a text note, persists it, and posts TextNoteSaved and SmartNotebookSaved.
     */
    fun addTextNoteToNotebook(
        notebook: SmartNotebook,
        noteText: String,
        context: Context
    ) {
        val app = context.applicationContext as? Application ?: return
        val repository: SmartNotebookRepository = get(SmartNotebookRepository::class.java)
        val textNotesDao: TextNotesDao = get(TextNotesDao::class.java)
        val bookId = notebook.smartBook.bookId ?: return
        val firstNote = notebook.atomicNotes.firstOrNull() ?: return

        firstNote.noteType = NoteType.TEXT_NOTE.toString()
        val atomicNotesDomain = get<AtomicNotesDomain>(AtomicNotesDomain::class.java)
        atomicNotesDomain.updateAtomicNote(firstNote)

        var entity = try {
            textNotesDao.getTextNoteForNote(firstNote.noteId)
        } catch (_: Exception) {
            null
        }
        if (entity == null) {
            entity = TextNoteEntity(firstNote.noteId, bookId)
            entity.noteText = noteText
            entity.createdTimeMillis = System.currentTimeMillis()
            entity.lastModifiedTimeMillis = System.currentTimeMillis()
            textNotesDao.insertTextNote(entity)
        } else {
            entity.noteText = noteText
            entity.lastModifiedTimeMillis = System.currentTimeMillis()
            textNotesDao.updateTextNote(entity)
        }

        org.greenrobot.eventbus.EventBus.getDefault().post(
            TextNoteSaved(bookId, firstNote, app)
        )
        repository.updateNotebook(notebook, app)
    }

    /**
     * Converts the first note to a handwritten note with minimal bitmap/strokes and posts HandwrittenNoteSaved.
     */
    fun addHandwrittenNoteToNotebook(
        notebook: SmartNotebook,
        context: Context
    ) {
        val app = context.applicationContext as? Application ?: return
        val repository: SmartNotebookRepository = get(SmartNotebookRepository::class.java)
        val handwrittenRepo: HandwrittenNoteRepository = get(HandwrittenNoteRepository::class.java)
        val bookId = notebook.smartBook?.bookId ?: return
        val firstNote = notebook.atomicNotes.firstOrNull() ?: return

        firstNote.noteType = NoteType.HANDWRITTEN_PNG.toString()
        val atomicNotesDomain = get<AtomicNotesDomain>(AtomicNotesDomain::class.java)
        atomicNotesDomain.updateAtomicNote(firstNote)

        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val stroke = Stroke(0xff000000.toInt(), 4f)
        stroke.addPoint(10f, 10f, 1f)
        stroke.addPoint(90f, 90f, 1f)
        val strokes = mutableListOf(stroke)

        handwrittenRepo.saveHandwrittenNotes(
            bookId,
            firstNote,
            bitmap,
            null,
            strokes,
            app
        )
        repository.updateNotebook(notebook, app)
    }

    /**
     * Deletes the given notebooks (DB + files) and posts NotebookDeleted.
     */
    fun deleteNotebooks(notebooks: List<SmartNotebook>) {
        val repository: SmartNotebookRepository = get(SmartNotebookRepository::class.java)
        for (n in notebooks) {
            repository.deleteSmartNotebook(n)
        }
    }

    /**
     * Finds all notebooks whose title contains [titlePrefix] and deletes them.
     */
    fun deleteNotebooksCreatedForTest(repository: SmartNotebookRepository, titlePrefix: String = TEST_SEED_TITLE_PREFIX) {
        val matching = repository.getSmartNotebooks(titlePrefix)
        for (n in matching) {
            repository.deleteSmartNotebook(n)
        }
    }
}
