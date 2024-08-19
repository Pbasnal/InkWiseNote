package com.example.hellodroid;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

public class ZoomableViewGroup extends ViewGroup {
    private float scaleFactor = 1.0f;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private float lastFocusX;
    private float lastFocusY;
    private float panX;
    private float panY;
    private OverScroller scroller;

    public ZoomableViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
        scroller = new OverScroller(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            child.layout(l, t, l + child.getMeasuredWidth(), t + child.getMeasuredHeight());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() > 0) {
            View child = getChildAt(0);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        int pointerCount = event.getPointerCount();
        float focusX = 0;
        float focusY = 0;

        for (int i = 0; i < pointerCount; i++) {
            focusX += event.getX(i);
            focusY += event.getY(i);
        }
        focusX /= pointerCount;
        focusY /= pointerCount;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                lastFocusX = focusX;
                lastFocusY = focusY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress()) {
                    float dx = focusX - lastFocusX;
                    float dy = focusY - lastFocusY;
                    panX += dx;
                    panY += dy;
                    invalidate();
                }
                lastFocusX = focusX;
                lastFocusY = focusY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(panX, panY);
        canvas.scale(scaleFactor, scaleFactor);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));
            invalidate();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            panX -= distanceX;
            panY -= distanceY;
            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            scroller.fling((int) panX, (int) panY, (int) velocityX, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            invalidate();
            return true;
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            panX = scroller.getCurrX();
            panY = scroller.getCurrY();
            invalidate();
        }
    }
}