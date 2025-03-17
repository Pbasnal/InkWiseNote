package com.originb.inkwisenote2.modules.smartnotes.ui

import android.util.Log
import androidx.recyclerview.widget.RecyclerView

class SmartNotebookScrollListener(private val scrollLayout: SmartNotebookPageScrollLayout) :
    RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        Log.d("Scroll state changed to ", "$dx---$dy")
        scrollLayout.setScrollRequested(false)
    }
}
