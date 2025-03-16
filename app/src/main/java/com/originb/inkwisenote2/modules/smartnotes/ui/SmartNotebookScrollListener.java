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
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        Log.d("Scroll state changed to ", "" + dx + "---" + dy);
        scrollLayout.setScrollRequested(false);
    }
}
