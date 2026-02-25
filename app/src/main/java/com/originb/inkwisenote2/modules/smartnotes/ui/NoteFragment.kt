package com.originb.inkwisenote2.modules.smartnotes.ui

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.originb.inkwisenote2.modules.backgroundjobs.Events.DeleteNoteCommand
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData
import org.greenrobot.eventbus.EventBus

abstract class NoteFragment(protected var smartNotebook: SmartNotebook?, var atomicNote: AtomicNoteEntity?) :
    Fragment() {
    /**
     * Shows a confirmation dialog before deleting a note
     */
    protected fun confirmDeleteNote() {
        if (getContext() == null) return

        AlertDialog.Builder(getContext()!!)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete", DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->
                val book = smartNotebook
                val note = atomicNote
                if (book != null && note != null) {
                    EventBus.getDefault().post(DeleteNoteCommand(book, note))
                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setNegativeButton("Cancel", null)
            .show()
    }

    abstract val noteHolderData: NoteHolderData?
}
