package com.originb.inkwisenote2.modules.smartnotes.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao

class SmartNotebookAdapter(
    private val parentActivity: AppCompatActivity,
    private var smartNotebook: SmartNotebook,
    private val smartNotebookRepository: SmartNotebookRepository,
    private val handwrittenNoteRepository: HandwrittenNoteRepository?,
    private val textNotesDao: TextNotesDao?,
    private val noteOcrTextDao: NoteOcrTextsDao?
) : RecyclerView.Adapter<FragmentViewHolder>() {
    // noteId to card mapping
    private val noteCards: MutableMap<Long?, FragmentViewHolder?> = HashMap<Long?, FragmentViewHolder?>()

    fun setSmartNotebook(smartNotebook: SmartNotebook) {
        this.smartNotebook = smartNotebook
        notifyDataSetChanged()
    }

    fun refreshFragments() {
        notifyDataSetChanged()
    }

    fun setSmartNotebook(smartNotebook: SmartNotebook, indexOfUpdatedNote: Int) {
        this.smartNotebook = smartNotebook
        notifyItemInserted(indexOfUpdatedNote)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FragmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_note_page, parent, false)

        // Create a truly unique ID for the fragment container
        val holder = FragmentViewHolder(
            this, view, parentActivity,
            handwrittenNoteRepository, textNotesDao,
            smartNotebookRepository, noteOcrTextDao
        )
        holder.fragmentContainer.setId(viewType + 1)

        return holder
    }

    override fun onBindViewHolder(holder: FragmentViewHolder, position: Int) {
        val atomicNotes: MutableList<AtomicNoteEntity> = smartNotebook.atomicNotes

        if (position < 0 || position >= atomicNotes.size) return

        val atomicNote = atomicNotes[position]
        holder.setNote(smartNotebook, atomicNotes[position], position)
        noteCards[atomicNote.noteId] = holder
    }

    fun updateNoteType(atomicNote: AtomicNoteEntity, newNoteType: String?) {

        val position: Int = smartNotebook.atomicNotes.indexOf(atomicNote)
        if (position == -1) {
            return
        }
        atomicNote.noteType = newNoteType
        execute { smartNotebookRepository.updateNotebook(smartNotebook, parentActivity) }
        notifyItemChanged(position)
    }

    fun removeNoteCard(noteId: Long) {
        if (!noteCards.containsKey(noteId)) return

        val position = noteCards[noteId]!!.getAdapterPosition()
        noteCards.remove(noteId)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return smartNotebook.atomicNotes.size
    }

    fun getNoteData(noteId: Long): NoteHolderData? {
        return noteCards[noteId]!!.noteHolderData
    }

    fun setNoteData(index: Int, currentNote: AtomicNoteEntity) {
        if (noteCards.containsKey(currentNote.noteId)) {
            noteCards[currentNote.noteId]!!.setNote(smartNotebook, currentNote, index)
        }
    }

    companion object {
        private val logger = Logger("SmartNotebookAdapter")
    }
}




































