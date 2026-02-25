package com.originb.inkwisenote2.modules.smartnotes.ui.activitystates

interface ISmartNotebookActivityState {
    fun initializeViews()

    fun finalizeState()

    fun setupObservers()

    fun saveNotebook()
}

