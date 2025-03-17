package com.originb.inkwisenote2.modules.backgroundjobs

import android.content.Context
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import lombok.AllArgsConstructor

class Events {
    open class EventData

    @AllArgsConstructor
    class NotebookDeleted : EventData() {
        var smartNotebook: SmartNotebook? = null
    }

    @AllArgsConstructor
    class NoteDeleted : EventData() {
        var smartNotebook: SmartNotebook? = null
        var atomicNote: AtomicNoteEntity? = null
    }

    @AllArgsConstructor
    class NoteStatus : EventData() {
        var smartNotebook: SmartNotebook? = null
        var status: TextProcessingStage? = null
    }

    @AllArgsConstructor
    class SmartNotebookSaved : EventData() {
        var smartNotebook: SmartNotebook? = null
        var context: Context? = null
    }
}
