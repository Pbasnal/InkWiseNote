package com.originb.inkwisenote.modules.backgroundjobs;

import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.smartnotes.data.AtomicNoteEntity;
import lombok.AllArgsConstructor;

public class Events {
    public static class EventData {
    }

    @AllArgsConstructor
    public static class NotebookDeleted extends EventData {
        public SmartNotebook smartNotebook;
    }

    @AllArgsConstructor
    public static class NoteDeleted extends EventData {
        public SmartNotebook smartNotebook;
        public AtomicNoteEntity atomicNote;
    }
}
