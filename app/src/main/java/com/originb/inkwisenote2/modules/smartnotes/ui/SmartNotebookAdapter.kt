package com.originb.inkwisenote2.modules.smartnotes.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.handwrittennotes.ui.HandwrittenNoteHolder
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import com.originb.inkwisenote2.modules.textnote.TextNoteHolder
import lombok.Setter

class SmartNotebookAdapter(
    private val parentActivity: ComponentActivity,
    @field:Setter private val smartNotebook: SmartNotebook?
) : RecyclerView.Adapter<NoteHolder>() {
    private val logger = Logger("SmartNotebookAdapter")

    private val smartNotebookRepository: SmartNotebookRepository =
        Repositories.Companion.getInstance().getSmartNotebookRepository()

    // noteId to card mapping
    private val noteCards: MutableMap<Long, NoteHolder> = HashMap()

    override fun getItemViewType(position: Int): Int {
        val atomicNote = smartNotebook.getAtomicNotes()[position]
        if (NoteType.NOT_SET.toString() == atomicNote.noteType) {
            return VIEW_TYPE_INIT
        } else if (NoteType.TEXT_NOTE.toString() == atomicNote.noteType) {
            return VIEW_TYPE_TEXT
        } else if (NoteType.HANDWRITTEN_PNG.toString() == atomicNote.noteType) {
            return VIEW_TYPE_HANDWRITTEN
        }
        return VIEW_TYPE_INIT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val itemView: View
        when (viewType) {
            VIEW_TYPE_TEXT -> {
                itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.note_text_layout, parent, false)
                return TextNoteHolder(itemView, parentActivity, smartNotebookRepository)
            }

            VIEW_TYPE_HANDWRITTEN -> {
                itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.note_drawing_layout, parent, false)
                return HandwrittenNoteHolder(itemView, parentActivity, smartNotebookRepository)
            }

            VIEW_TYPE_INIT -> {
                itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.note_init_layout, parent, false)
                return InitNoteHolder(itemView, parentActivity, smartNotebookRepository, this)
            }

            else -> {
                itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.note_init_layout, parent, false)
                return InitNoteHolder(itemView, parentActivity, smartNotebookRepository, this)
            }
        }
    }

    override fun onBindViewHolder(noteHolder: NoteHolder, position: Int) {
        val atomicNote = smartNotebook.getAtomicNotes()[position]
        noteHolder.setNote(smartNotebook.getSmartBook().bookId, atomicNote)
        noteCards[atomicNote.noteId] = noteHolder
    }

    override fun onViewRecycled(holder: NoteHolder) {
        super.onViewRecycled(holder)
        if (holder is HandwrittenNoteHolder || holder is TextNoteHolder) {
            holder.saveNote()
        }
    }

    fun updateNoteType(atomicNote: AtomicNoteEntity, newNoteType: String?) {
        val position = smartNotebook.getAtomicNotes().indexOf(atomicNote)
        if (position != -1) {
            atomicNote.noteType = newNoteType
            BackgroundOps.Companion.execute(Runnable { smartNotebookRepository.updateNotebook(smartNotebook) })
            notifyItemChanged(position)
        }
    }

    fun saveNote(noteTitle: String?) {
        if (smartNotebook == null) return
        BackgroundOps.Companion.execute(Runnable {
            for (noteHolder in noteCards.values) {
                noteHolder.saveNote()
            }
            // update title
            smartNotebook.getSmartBook().title = noteTitle
            smartNotebookRepository.updateNotebook(smartNotebook)
        })
    }

    fun removeNoteCard(noteId: Long) {
        val position = noteCards[noteId]!!.adapterPosition
        noteCards.remove(noteId)
        notifyItemRemoved(position)
    }

    // this function assumes that either the smartNotebook has updated
    // notes and pages or
    // all new notes or pages are inserted after current index so that
    // the note and page at this index is not affected.
    fun saveNotebookPageAt(currentVisibleItemIndex: Int, atomicNote: AtomicNoteEntity?) {
        if (!noteCards.containsKey(atomicNote.getNoteId())) {
            return
        }

        val noteHolder = noteCards[atomicNote.getNoteId()]
        BackgroundOps.Companion.execute(Runnable { noteHolder!!.saveNote() })
    }

    override fun getItemCount(): Int {
        return if (smartNotebook != null) smartNotebook.atomicNotes!!.size else 0
    }

    companion object {
        private const val VIEW_TYPE_INIT = 0
        private const val VIEW_TYPE_TEXT = 1
        private const val VIEW_TYPE_HANDWRITTEN = 2
    }
}
