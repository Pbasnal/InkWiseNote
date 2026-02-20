package com.originb.inkwisenote2.modules.smartnotes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType

class InitNoteFragment(
    smartNotebook: SmartNotebook?,
    atomicNote: AtomicNoteEntity?,
    private val adapter: SmartNotebookAdapter
) : NoteFragment(smartNotebook, atomicNote) {
    private var cardToHandwriting: CardView? = null
    private var cardToText: CardView? = null

    private var deleteNote: ImageButton? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val itemView = inflater.inflate(R.layout.note_init_fragment, container, false)

        cardToHandwriting = itemView.findViewById<CardView>(R.id.touch_to_write)
        cardToHandwriting!!.setOnClickListener(View.OnClickListener { view: View? -> this.createHandwrittenNote(view) })

        cardToText = itemView.findViewById<CardView>(R.id.tap_to_text)
        cardToText!!.setOnClickListener(View.OnClickListener { view: View? -> this.createTextNote(view) })

        deleteNote = itemView.findViewById<ImageButton>(R.id.delete_note)

        // TODO: What happens if the 2nd last not was deleted while this note was visible on screen
        if (smartNotebook.getAtomicNotes().size() <= 1) {
            deleteNote!!.setVisibility(View.GONE)
        } else {
            deleteNote!!.setVisibility(View.VISIBLE)
        }

        deleteNote!!.setOnClickListener(View.OnClickListener { v: View? -> confirmDeleteNote() }
        )

        return itemView
    }

    private fun createTextNote(view: View?) {
        if (atomicNote == null) return
        adapter.updateNoteType(atomicNote, NoteType.TEXT_NOTE.toString())
    }

    private fun createHandwrittenNote(view: View?) {
        if (atomicNote == null) return
        adapter.updateNoteType(atomicNote, NoteType.HANDWRITTEN_PNG.toString())
    }

    override fun getNoteHolderData(): NoteHolderData {
        return NoteHolderData.Companion.initNoteData()
    }
}
