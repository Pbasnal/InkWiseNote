package com.originb.inkwisenote2.common

import android.content.Context
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.ui.NoteDebugDialog
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao

/**
 * Shows the note debug dialog if all required dependencies are non-null.
 * No-op if context, repo, textDao, or ocrDao is null.
 */
fun showDebugDialog(
    context: Context?,
    atomicNote: AtomicNoteEntity?,
    smartNotebook: SmartNotebook?,
    smartNotebookRepository: SmartNotebookRepository?,
    textNotesDao: TextNotesDao?,
    noteOcrTextDao: NoteOcrTextsDao?,
    handwrittenNoteRepository: HandwrittenNoteRepository?
) {
    val ctx = context ?: return
    val repo = smartNotebookRepository ?: return
    val textDao = textNotesDao ?: return
    val ocrDao = noteOcrTextDao ?: return
    val dialog = NoteDebugDialog(
        ctx, atomicNote, smartNotebook,
        repo, textDao, ocrDao, handwrittenNoteRepository
    )
    dialog.show()
}
