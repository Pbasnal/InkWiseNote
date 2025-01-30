package com.originb.inkwisenote.data.entities.tasks;

public class TfIdfRelationTasks {
    public static NoteTaskStatus newTask(long noteId) {
        return new NoteTaskStatus(noteId, NoteTaskName.TF_IDF_RELATION, NoteTaskStage.TEXT_PARSING);
    }

    public static NoteTaskStatus tokenizationTask(Long noteId) {
        return new NoteTaskStatus(noteId, NoteTaskName.TF_IDF_RELATION, NoteTaskStage.TOKENIZATION);
    }

    public static NoteTaskStatus completeTask(Long noteId) {
        return new NoteTaskStatus(noteId, NoteTaskName.TF_IDF_RELATION, NoteTaskStage.NOTE_READY);
    }
}
