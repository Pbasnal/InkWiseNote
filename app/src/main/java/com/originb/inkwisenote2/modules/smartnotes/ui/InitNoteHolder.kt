package com.originb.inkwisenote2.modules.smartnotes.ui

import android.view.View
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType

class InitNoteHolder(
    itemView: View,
    parentActivity: ComponentActivity?,
    smartNotebookRepository: SmartNotebookRepository?,
    private val adapter: SmartNotebookAdapter
) : NoteHolder(itemView, parentActivity, smartNotebookRepository) {
    private val logger = Logger("InitNoteHolder")
    private val cardToHandwriting: CardView = itemView.findViewById(R.id.touch_to_write)
    private val cardToText: CardView

    private var atomicNote: AtomicNoteEntity? = null

    init {
        cardToHandwriting.setOnClickListener { view: View -> this.createHandwrittenNote(view) }

        cardToText = itemView.findViewById(R.id.tap_to_text)
        cardToText.setOnClickListener { view: View -> this.createTextNote(view) }
    }

    private fun createTextNote(view: View) {
        if (atomicNote == null) return
        adapter.updateNoteType(atomicNote!!, NoteType.TEXT_NOTE.toString())
    }

    private fun createHandwrittenNote(view: View) {
        if (atomicNote == null) return
        adapter.updateNoteType(atomicNote!!, NoteType.HANDWRITTEN_PNG.toString())
    }

    override fun setNote(bookId: Long, atomicNote: AtomicNoteEntity) {
        this.atomicNote = atomicNote
        logger.debug("Setting note")
    }

    override fun saveNote(): Boolean {
        return false
    }
}
