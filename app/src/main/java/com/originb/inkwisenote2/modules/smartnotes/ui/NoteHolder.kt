package com.originb.inkwisenote2.modules.smartnotes.ui

import android.view.View
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity

abstract class NoteHolder(
    itemView: View?,
    protected val parentActivity: ComponentActivity?,
    protected val smartNotebookRepository: SmartNotebookRepository?
) : RecyclerView.ViewHolder(
    itemView!!
) {
    abstract fun setNote(bookId: Long, atomicNote: AtomicNoteEntity)

    abstract fun saveNote(): Boolean
}
