package com.originb.inkwisenote2.modules.backgroundjobs

import android.content.Context
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.queries.data.QueryEntity
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity

class Events {
    open class EventData

    class DeleteNotebookCommand(var smartNotebook: SmartNotebook?) : EventData()

    class DeleteNoteCommand(var smartNotebook: SmartNotebook?, var atomicNote: AtomicNoteEntity?) : EventData()

    class NotebookDeleted(var smartNotebook: SmartNotebook?) : EventData()

    class NoteDeleted(var smartNotebook: SmartNotebook?, var atomicNote: AtomicNoteEntity?) : EventData()

    class NoteStatus(var bookId: Long, var status: TextProcessingStage?) : EventData()

    class SmartNotebookSaved(var smartNotebook: SmartNotebook?, var context: Context?) : EventData()

    class HandwrittenNoteSaved(var bookId: Long, var atomicNote: AtomicNoteEntity?, var context: Context?) : EventData()

    class TextNoteSaved(var bookId: Long, var atomicNote: AtomicNoteEntity?, var context: Context?) : EventData()

    class QueryUpdated(var query: QueryEntity?) : EventData()

    class QueryDeleted(var query: QueryEntity?) : EventData()
}
