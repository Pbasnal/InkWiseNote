package com.originb.inkwisenote2.modules.smartnotes.data;

public enum SmartNotebookUpdateType {
    NOTE_UPDATE(0),
    NOTE_DELETED(1),
    NOTEBOOK_DELETED(2),
    NOTEBOOK_TITLE_UPDATED(3),;

    private final int updateType;

    SmartNotebookUpdateType(int updateType) {
        this.updateType = updateType;
    }

    public int toInt() {
        return updateType;
    }

    @Override
    public String toString() {
        return String.valueOf(updateType);
    }

    public boolean equals(int updateType) {
        return this.updateType == updateType;
    }
}
