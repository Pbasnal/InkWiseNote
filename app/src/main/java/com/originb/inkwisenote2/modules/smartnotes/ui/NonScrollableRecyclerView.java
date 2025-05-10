package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class NonScrollableRecyclerView extends RecyclerView {
    public NonScrollableRecyclerView(@NonNull Context context) {
        super(context);
    }

    public NonScrollableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NonScrollableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        // Prevent gesture-based scrolling
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // Prevent gesture-based scrolling
        return false;
    }
}
