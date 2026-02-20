package com.originb.inkwisenote2.modules.smartnotes.ui

import android.util.Log
import androidx.recyclerview.widget.RecyclerView

class SmartNotebookScrollListener(private val scrollLayout: SmartNotebookPageScrollLayout) :
    RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        Log.d("Scrolling", "Scroll state change")
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        scrollLayout.setScrollRequested(false)
    }
}
