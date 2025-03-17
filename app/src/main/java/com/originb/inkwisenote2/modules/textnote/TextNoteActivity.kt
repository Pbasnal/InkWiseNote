package com.originb.inkwisenote2.modules.textnote

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NotebookDeleted
import com.originb.inkwisenote2.modules.backgroundjobs.Events.SmartNotebookSaved
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer

class TextNoteActivity : AppCompatActivity() {
    private var noteEditText: EditText? = null
    private var noteTitle: EditText? = null
    private var deleteBtn: ImageButton? = null

    private var workingNotePath: String? = null
    private var smartNotebookRepository: SmartNotebookRepository? = null
    private var textNotesDao: TextNotesDao? = null

    private var textNoteEntity: TextNoteEntity? = null
    private var notebook: SmartNotebook? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_note)

        smartNotebookRepository = Repositories.Companion.getInstance().getSmartNotebookRepository()
        textNotesDao = Repositories.Companion.getInstance().getNotesDb().textNotesDao()

        noteEditText = findViewById(R.id.note_edit_text)
        noteTitle = findViewById(R.id.text_note_title)
        deleteBtn = findViewById(R.id.delete_note)

        BackgroundOps.Companion.execute<Optional<SmartNotebook>?>(
            Callable<Optional<SmartNotebook?>?> { this.smartNotebook },
            Consumer<Optional<SmartNotebook?>?> { smartNotebook: Optional<SmartNotebook?>? ->
                notebook = smartNotebook!!.get()
                noteEditText.setText(textNoteEntity.getNoteText())
                noteTitle.setText(notebook!!.smartBook.title)
                deleteBtn.setOnClickListener(View.OnClickListener { view: View? ->
                    EventBus.getDefault().post(NotebookDeleted(notebook))
                    Routing.HomePageActivity.openHomePageAndStartFresh(this)
                })
            })
    }

    private val smartNotebook: Optional<SmartNotebook?>?
        get() {
            val bookIdToOpen = intent.getLongExtra("bookId", -1)
            workingNotePath = intent.getStringExtra("workingNotePath")
            val notebookOpt = if (bookIdToOpen != -1L) {
                smartNotebookRepository!!.getSmartNotebooks(bookIdToOpen)
            } else {
                smartNotebookRepository!!.initializeNewSmartNotebook(
                    "",
                    workingNotePath,
                    NoteType.TEXT_NOTE
                )
            }

            notebookOpt.ifPresent { notebook: SmartNotebook? ->
                textNoteEntity = textNotesDao!!.getTextNoteForBook(notebook.getSmartBook().bookId)
                if (textNoteEntity == null) {
                    textNoteEntity = TextNoteEntity(
                        notebook.getAtomicNotes()[0].noteId,
                        notebook!!.smartBook.bookId
                    )
                    textNotesDao!!.insertTextNote(textNoteEntity)
                }
            }

            return notebookOpt
        }

    override fun onBackPressed() {
        super.onBackPressed()

        BackgroundOps.Companion.execute(Runnable {
            textNoteEntity.setNoteText(noteEditText!!.text.toString())
            textNotesDao!!.updateTextNote(textNoteEntity)
        })

        EventBus.getDefault().post(SmartNotebookSaved(notebook, this))
    }
}
