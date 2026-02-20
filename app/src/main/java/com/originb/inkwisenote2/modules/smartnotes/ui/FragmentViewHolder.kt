package com.originb.inkwisenote2.modules.smartnotes.ui

import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.get

internal class FragmentViewHolder(
    var adapter: SmartNotebookAdapter?, itemView: View,
    parentActivity: AppCompatActivity,
    var handwrittenNoteRepository: HandwrittenNoteRepository?,
    var textNotesDao: TextNotesDao?,
    var smartNotebookRepository: SmartNotebookRepository?,
    var noteOcrTextDao: NoteOcrTextsDao?
) : RecyclerView.ViewHolder(itemView) {
    private val logger = Logger(FragmentViewHolder::class.java.getName())

    var fragmentContainer: FrameLayout
    var noteFragment: NoteFragment? = null
    var fragmentManager: FragmentManager

    init {
        fragmentContainer = itemView.findViewById<FrameLayout>(R.id.note_fragment_container)
        this.fragmentManager = parentActivity.getSupportFragmentManager()
    }

    fun setNote(notebook: SmartNotebook?, atomicNote: AtomicNoteEntity, position: Int) {
        if (isCorrectFragmentAttached(atomicNote)
            && atomicNote.getNoteId() == noteFragment!!.atomicNote.getNoteId()
        ) return

        noteFragment = this.createFragmentByType(notebook, atomicNote, adapter)

        val containerId = fragmentContainer.getId()
        itemView.post(Runnable {
            if (!itemView.isAttachedToWindow()) {
                logger.debug("View hasn't attached to window")
                return@post
            }
            val transaction = fragmentManager.beginTransaction()

            // Remove any existing fragment in this container
            val existingFragment = fragmentManager.findFragmentById(containerId)
            if (existingFragment != null && existingFragment !== noteFragment) {
                transaction.remove(existingFragment)
            }
            val fragmentTag = "fragment_" + position
            // Add the fragment to this container
            if (noteFragment!!.isDetached()) {
                transaction.attach(noteFragment!!)
            } else if (!noteFragment!!.isAdded()) {
                transaction.add(containerId, noteFragment!!, fragmentTag)
            } else {
                transaction.replace(containerId, noteFragment!!, fragmentTag)
            }
            try {
                transaction.commitNowAllowingStateLoss()
            } catch (ex: Exception) {
                logger.exception("Failed to commit fragment transaction", ex)
            }
        })
    }

    private fun isCorrectFragmentAttached(atomicNote: AtomicNoteEntity): Boolean {
        if (noteFragment == null) return false

        val noteHolderData = noteFragment!!.getNoteHolderData()

        when (noteHolderData.noteType) {
            NoteType.TEXT_NOTE -> return NoteType.TEXT_NOTE.equals(atomicNote.getNoteType())
            NoteType.HANDWRITTEN_PNG -> return NoteType.HANDWRITTEN_PNG.equals(atomicNote.getNoteType())
            NoteType.NOT_SET -> return NoteType.NOT_SET.equals(atomicNote.getNoteType())
            else -> return false
        }
    }

    fun createFragmentByType(
        notebook: SmartNotebook?,
        atomicNote: AtomicNoteEntity,
        adapter: SmartNotebookAdapter?
    ): NoteFragment? {
        val noteType: NoteType = NoteType.Companion.fromString(atomicNote.getNoteType())
        when (noteType) {
            NoteType.TEXT_NOTE -> return get<NoteFragment?>(TextNoteFragment::class.java, null) {
                parametersOf(
                    notebook,
                    atomicNote
                )
            }

            NoteType.HANDWRITTEN_PNG -> return get<NoteFragment?>(
                HandwrittenNoteFragment::class.java,
                null
            ) { parametersOf(notebook, atomicNote) }

            else -> return get<NoteFragment?>(InitNoteFragment::class.java, null) {
                parametersOf(
                    notebook,
                    atomicNote,
                    adapter
                )
            }
        }
    }

    val noteHolderData: NoteHolderData
        get() = noteFragment!!.getNoteHolderData()
}
