package com.originb.inkwisenote.modules.smartnotes.ui;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import lombok.Setter;

public class SmartNotebookPageScrollLayout extends LinearLayoutManager {

    @Setter
    private boolean scrollRequested;

    public SmartNotebookPageScrollLayout(Context context) {
        super(context, LinearLayoutManager.HORIZONTAL, false);
        scrollRequested = false;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public boolean canScrollHorizontally() {
        return scrollRequested;
    }
}
