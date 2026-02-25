package com.originb.inkwisenote2.modules.smartnotes.ui

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
class SmartNotebookPageScrollLayout(context: Context?) : LinearLayoutManager(context, HORIZONTAL, false) {
    private var scrollRequested = false

    fun setScrollRequested(requested: Boolean) {
        scrollRequested = requested
    }

    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun canScrollHorizontally(): Boolean {
        return scrollRequested
    }
}
