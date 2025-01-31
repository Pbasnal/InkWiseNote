package com.originb.inkwisenote.data.entities.tasks;

public enum NoteTaskName {
    TF_IDF_RELATION("TF_IDF_RELATION");

    private final String noteTaskType;

    NoteTaskName(String noteTaskType) {
        this.noteTaskType = noteTaskType;
    }

    @Override
    public String toString() {
        return noteTaskType;
    }
}