package com.originb.inkwisenote2.modules.noterelation.ui

import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteWithImage
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNoteGridAdapter
import lombok.Getter
import lombok.Setter
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer
import java.util.stream.Collectors

class RelatedNotesActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var smartNoteGridAdapter: SmartNoteGridAdapter? = null

    private var smartNotebookRepository: SmartNotebookRepository? = null
    private var handwrittenNoteRepository: HandwrittenNoteRepository? = null
    private var noteRelationRepository: NoteRelationRepository? = null

    private var noteRelationDao: NoteRelationDao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_related_notes)

        smartNotebookRepository = Repositories.Companion.getInstance().getSmartNotebookRepository()
        handwrittenNoteRepository = Repositories.Companion.getInstance().getHandwrittenNoteRepository()
        noteRelationRepository = Repositories.Companion.getInstance().getNoteRelationRepository()
        noteRelationDao = Repositories.Companion.getInstance().getNotesDb().noteRelationDao()

        val rootBookId = intent.getLongExtra("book_id", 0)

        BackgroundOps.Companion.execute<NotesDataOfFirstNote>(
            Callable<NotesDataOfFirstNote> {
                val smartNotebook = getRootBook(rootBookId)
                val noteIds = smartNotebook.atomicNotes!!.stream().map { obj: AtomicNoteEntity? -> obj.getNoteId() }
                    .collect(Collectors.toSet())
                val noteRelations = noteRelationDao!!.getRelatedNotesOf(noteIds)
                val allBookIds = noteRelations!!.stream()
                    .map { obj: NoteRelation? -> obj.getBookId() }.collect(Collectors.toSet())
                allBookIds.addAll(
                    noteRelations!!.stream()
                        .map { obj: NoteRelation? -> obj.getRelatedBookId() }.collect(Collectors.toSet())
                )
                allBookIds.remove(smartNotebook.getSmartBook().bookId)
                val allBooks =
                    allBookIds.stream().map { bookId: Long -> smartNotebookRepository!!.getSmartNotebooks(bookId) }
                        .filter { obj: Optional<SmartNotebook?>? -> obj!!.isPresent }
                        .map { obj: Optional<SmartNotebook?>? -> obj!!.get() }
                        .collect(Collectors.toList())

                val firstNote = smartNotebook.getAtomicNotes()[0]
                val handwrittenNoteWithImage =
                    handwrittenNoteRepository!!.getNoteImage(firstNote, BitmapScale.THUMBNAIL)

                val notesDataOfFirstNote = NotesDataOfFirstNote()
                notesDataOfFirstNote.setSmartNotebook(smartNotebook)
                notesDataOfFirstNote.setHandwrittenNoteWithImage(handwrittenNoteWithImage)
                notesDataOfFirstNote.setNoteRelations(HashSet<NoteRelation?>(noteRelations))
                notesDataOfFirstNote.setAllBooksToShow(allBooks)
                notesDataOfFirstNote
            },
            Consumer<NotesDataOfFirstNote> { notesDataOfFirstNote: NotesDataOfFirstNote ->
                setRootNote(notesDataOfFirstNote)
                smartNoteGridAdapter!!.updateNoteRelations(notesDataOfFirstNote.getNoteRelations())
                smartNoteGridAdapter!!.setSmartNotebooks(notesDataOfFirstNote.getAllBooksToShow())
            })

        createGridLayoutToShowNotes()
    }

    private fun getRootBook(bookId: Long): SmartNotebook {
        val noteEntityOpt = smartNotebookRepository!!.getSmartNotebooks(bookId)
        return noteEntityOpt!!.get()
    }

    private fun setRootNote(notesDataOfFirstNote: NotesDataOfFirstNote) {
        val includedCard = findViewById<View>(R.id.main_note_card)

        // Then access its child views
        val cardImage = includedCard.findViewById<ImageView>(R.id.card_image)
        val cardTitle = includedCard.findViewById<TextView>(R.id.card_name)
        val deleteButton = includedCard.findViewById<ImageButton>(R.id.btn_dlt_note)

        notesDataOfFirstNote.getHandwrittenNoteWithImage().noteImage
            .ifPresent(Consumer<Bitmap> { bm: Bitmap? -> cardImage.setImageBitmap(bm) })
        val smartBook = notesDataOfFirstNote.smartNotebook.getSmartBook()

        val noteTitle = Optional.ofNullable(smartBook.title)
            .filter { title: String? -> !title!!.trim { it <= ' ' }.isEmpty() }
            .orElse(DateTimeUtils.msToDateTime(smartBook.lastModifiedTimeMillis))
        cardTitle.text = noteTitle

        cardImage.setOnClickListener { v: View? ->
            Routing.SmartNotebookActivity.openNotebookIntent(
                this, filesDir.path, smartBook.bookId
            )
        }
        cardTitle.setOnClickListener { v: View? ->
            Routing.SmartNotebookActivity.openNotebookIntent(
                this, filesDir.path, smartBook.bookId
            )
        }

        deleteButton.setOnClickListener { v: View? ->
            notesDataOfFirstNote.smartNotebook!!.atomicNotes!!.forEach(Consumer { note: AtomicNoteEntity? ->
                handwrittenNoteRepository!!.deleteHandwrittenNote(note)
                noteRelationRepository!!.deleteNoteRelationData(note)
            })
            smartNotebookRepository!!.deleteSmartNotebook(notesDataOfFirstNote.smartNotebook)
            Routing.HomePageActivity.openHomePageAndStartFresh(this)
        }
    }

    fun createGridLayoutToShowNotes() {
        recyclerView = findViewById(R.id.related_note_card_grid_view)
        val gridLayoutManager = GridLayoutManager(this, 2)
        recyclerView.setLayoutManager(gridLayoutManager)

        smartNoteGridAdapter = SmartNoteGridAdapter(this, ArrayList())

        recyclerView.setAdapter(smartNoteGridAdapter)
        recyclerView.setHasFixedSize(true)
    }

    @Getter
    @Setter
    class NotesDataOfFirstNote {
        val smartNotebook: SmartNotebook? = null
        private val handwrittenNoteWithImage: HandwrittenNoteWithImage? = null
        private val noteRelations: Set<NoteRelation>? = null
        private val allBooksToShow: List<SmartNotebook>? = null
    }
}