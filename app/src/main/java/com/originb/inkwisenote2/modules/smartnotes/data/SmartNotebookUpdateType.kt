package com.originb.inkwisenote2.modules.smartnotes.data

enum class SmartNotebookUpdateType(private val updateType: Int) {
    NOTE_UPDATE(0),
    NOTE_DELETED(1),
    NOTEBOOK_DELETED(2),
    NOTEBOOK_TITLE_UPDATED(3),
    ;

    fun toInt(): Int {
        return updateType
    }

    override fun toString(): String {
        return updateType.toString()
    }

    fun equals(updateType: Int): Boolean {
        return this.updateType == updateType
    }
}
