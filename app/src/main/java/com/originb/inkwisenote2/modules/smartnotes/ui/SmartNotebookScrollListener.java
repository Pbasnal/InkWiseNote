package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SmartNotebookScrollListener extends RecyclerView.OnScrollListener {
    private SmartNotebookPageScrollLayout scrollLayout;

    public SmartNotebookScrollListener(SmartNotebookPageScrollLayout scrollLayout) {
        this.scrollLayout = scrollLayout;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        Log.d("Scrolling", "Scroll state change");
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        scrollLayout.setScrollRequested(false);
    }
}
