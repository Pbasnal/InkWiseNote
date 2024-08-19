package com.originb.inkwisenote;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawingViewOld extends View {

    private Paint paint;
    private Path path;
    private Bitmap bitmap;
    private Canvas canvas;

    // Constructor 1: Used when creating the view programmatically
    public DrawingViewOld(Context context) {
        super(context);
        init();
    }

    // Constructor 2: Used when inflating the view from XML
    public DrawingViewOld(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Constructor 3: Used when inflating the view from XML with a style attribute
    public DrawingViewOld(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        path = new Path();

//        bitmap = Bitmap.createBitmap(800, 1280, Bitmap.Config.ARGB_8888);
//        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Create a bitmap with the new dimensions
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        // Create a new canvas to draw on the bitmap
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                canvas.drawPath(path, paint);
                path.reset();
                invalidate();
                break;
        }

        return true;
    }
}
