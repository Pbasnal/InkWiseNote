package com.originb.inkwisenote2.modules.smartnotes.ui

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import lombok.Setter

class SmartNotebookPageScrollLayout(context: Context?) : LinearLayoutManager(context, HORIZONTAL, false) {
    @Setter
    private val scrollRequested = false

    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun canScrollHorizontally(): Boolean {
        return scrollRequested
    }
}
