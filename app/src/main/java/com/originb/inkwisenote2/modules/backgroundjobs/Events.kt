package com.originb.inkwisenote2.modules.backgroundjobs

import android.content.Context
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.queries.data.QueryEntity
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity

class Events {
    open class EventData

    class DeleteNotebookCommand(@JvmField var smartNotebook: SmartNotebook?) : EventData()

    class DeleteNoteCommand(@JvmField var smartNotebook: SmartNotebook?, @JvmField var atomicNote: AtomicNoteEntity?) : EventData()

    class NotebookDeleted(@JvmField var smartNotebook: SmartNotebook?) : EventData()

    class NoteDeleted(var smartNotebook: SmartNotebook?, @JvmField var atomicNote: AtomicNoteEntity?) : EventData()

    class NoteStatus(@JvmField var bookId: Long, @JvmField var status: TextProcessingStage?) : EventData()

    class SmartNotebookSaved(@JvmField var smartNotebook: SmartNotebook?, var context: Context?) : EventData()

    class HandwrittenNoteSaved(@JvmField var bookId: Long, @JvmField var atomicNote: AtomicNoteEntity?, @JvmField var context: Context?) : EventData()

    class TextNoteSaved(@JvmField var bookId: Long, @JvmField var atomicNote: AtomicNoteEntity?, @JvmField var context: Context?) : EventData()

    class QueryUpdated(@JvmField var query: QueryEntity?) : EventData()

    class QueryDeleted(@JvmField var query: QueryEntity?) : EventData()
}
