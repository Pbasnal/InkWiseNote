package com.originb.inkwisenote2.modules.backgroundjobs;

import android.content.Context;
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity;
import lombok.AllArgsConstructor;

public class Events {
    public static class EventData {
    }

    @AllArgsConstructor
    public static class DeleteNotebookCommand extends EventData {
        public SmartNotebook smartNotebook;
    }

    @AllArgsConstructor
    public static class DeleteNoteCommand extends EventData {
        public SmartNotebook smartNotebook;
        public AtomicNoteEntity atomicNote;
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

    @AllArgsConstructor
    public static class NoteStatus extends EventData {
//        public SmartNotebook smartNotebook;
        public long bookId;
        public TextProcessingStage status;
    }

    @AllArgsConstructor
    public static class SmartNotebookSaved extends EventData {
        public SmartNotebook smartNotebook;
        public Context context;
    }

    @AllArgsConstructor
    public static class HandwrittenNoteSaved extends EventData {
        public long bookId;
        public AtomicNoteEntity atomicNote;
        public Context context;
    }


    @AllArgsConstructor
    public static class TextNoteSaved extends EventData {
        public long bookId;
        public AtomicNoteEntity atomicNote;
        public Context context;
    }

    @AllArgsConstructor
    public static class QueryUpdated extends EventData {
        public QueryEntity query;
    }

    @AllArgsConstructor
    public static class QueryDeleted extends EventData {
        public QueryEntity query;
    }
}
