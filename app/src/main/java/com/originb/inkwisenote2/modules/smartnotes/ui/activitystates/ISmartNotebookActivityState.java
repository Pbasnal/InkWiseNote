package com.originb.inkwisenote2.modules.smartnotes.ui.activitystates;

public interface ISmartNotebookActivityState {
    void initializeViews();

    void finalizeState();

    void setupObservers();

    void saveNotebook();
}

