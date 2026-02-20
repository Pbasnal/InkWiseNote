package com.originb.inkwisenote2.modules.smartnotes.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class NonScrollableRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        // Prevent gesture-based scrolling
        return false
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        // Prevent gesture-based scrolling
        return false
    }
}
