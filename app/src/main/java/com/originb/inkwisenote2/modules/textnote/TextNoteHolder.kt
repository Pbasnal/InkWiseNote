package com.originb.inkwisenote2.modules.textnote

import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NotebookDeleted
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.ui.NoteHolder
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer

class TextNoteHolder(
    itemView: View,
    parentActivity: ComponentActivity?,
    smartNotebookRepository: SmartNotebookRepository?
) : NoteHolder(itemView, parentActivity, smartNotebookRepository) {
    private val noteEditText: EditText = itemView.findViewById(R.id.note_edit_text)
    private val deleteBtn: ImageButton = itemView.findViewById(R.id.delete_note)


    private val textNotesDao: TextNotesDao =
        Repositories.Companion.getInstance().getNotesDb().textNotesDao()

    private var textNoteEntity: TextNoteEntity? = null
    private var notebook: SmartNotebook? = null

    override fun setNote(bookId: Long, atomicNote: AtomicNoteEntity) {
        BackgroundOps.Companion.execute<Optional<SmartNotebook>?>(
            Callable<Optional<SmartNotebook?>?> { getSmartNotebook(bookId) },
            Consumer<Optional<SmartNotebook?>?> { smartNotebookOpt: Optional<SmartNotebook?>? ->
                notebook = smartNotebookOpt!!.get()
                noteEditText.setText(textNoteEntity.getNoteText())
                deleteBtn.setOnClickListener { view: View? ->
                    EventBus.getDefault().post(NotebookDeleted(notebook))
                    Routing.HomePageActivity.openHomePageAndStartFresh(parentActivity!!)
                }
            })
    }

    private fun getSmartNotebook(bookId: Long): Optional<SmartNotebook?>? {
        val notebookOpt = smartNotebookRepository!!.getSmartNotebooks(bookId)

        notebookOpt!!.ifPresent { notebook: SmartNotebook? ->
            textNoteEntity = textNotesDao.getTextNoteForBook(notebook.getSmartBook().bookId)
            if (textNoteEntity == null) {
                textNoteEntity = TextNoteEntity(
                    notebook.getAtomicNotes()[0].noteId,
                    notebook!!.smartBook.bookId
                )
                textNotesDao.insertTextNote(textNoteEntity)
            }
        }

        return notebookOpt
    }

    override fun saveNote(): Boolean {
        BackgroundOps.Companion.execute(Runnable {
            textNoteEntity.setNoteText(noteEditText.text.toString())
            textNotesDao.updateTextNote(textNoteEntity)
        })
        return true
    }
}
