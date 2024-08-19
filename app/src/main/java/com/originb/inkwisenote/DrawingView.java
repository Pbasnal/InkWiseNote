package com.originb.inkwisenote;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DrawingView extends View {
    public Bitmap bitmap;
    private Canvas bitmapCanvas;


    private Paint paint;
    private MyPath path;
    private List<MyPath> paths;
    private List<Paint> paints;
    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.0f;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize paint
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);

        path = new MyPath();
        paths = new ArrayList<>();
        paints = new ArrayList<>();

        bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);


        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas(newBitmap);

            // Draw the old bitmap onto the new one
            newCanvas.drawBitmap(bitmap, 0, 0, null);

            bitmap = newBitmap;
            bitmapCanvas = newCanvas;
        }
    }

    public List<MyPath> getPaths() {
        return paths;
    }

    public List<Paint> getPaints() {
        return paints;
    }

    public void setPaths(List<MyPath> paths, List<Note.PaintData> paints) {
        this.paths = paths;
        for (MyPath myPath : this.paths) {
            myPath.loadThisPath();
        }
        Log.w("inkWise", "Number of paths: " + (long) this.paths.size());

        this.paints = paints.stream().map(Note.PaintData::toPaint).collect(Collectors.toList());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);
        // Draw the cached bitmap
        canvas.drawBitmap(bitmap, 0, 0, null);
//        //TODO: use a combination of bitmap and paths
//        for (int i = 0; i < paths.size(); i++) {
//            canvas.drawPath(paths.get(i), paints.get(i));
//        }
        canvas.drawPath(path, paint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        if (!scaleDetector.isInProgress()) {
            float x = event.getX() / scaleFactor;
            float y = event.getY() / scaleFactor;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    bitmapCanvas.drawPath(path, paint); // Draw the current path onto the bitmap
                    path = new MyPath(); // Create a new path for the next touch event
                    break;
                default:
                    return false;
            }
        }

        invalidate();
        return true;
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
}