package com.originb.inkwisenote.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import com.originb.inkwisenote.data.views.WriteablePath;
import com.originb.inkwisenote.data.Note;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class DrawingView extends View {
    /*
     * The bitmap is used to cache the drawing so that it doesn't have to be redrawn every time onDraw is called.
     * The bitmapCanvas is used to draw the paths onto the bitmap.
     *
     * user draws a path on the screen --> updates path --> bitmapCanvas updates bitmap
     * onDraw is called --> canvas draws updated bitmap
     *                  --> canvas draws updated path. [this is the path that user is currently drawing]
     * */
    public Bitmap bitmap;
    private Canvas bitmapCanvas;


    private Paint paint;
    private WriteablePath path;
    private List<WriteablePath> paths;
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

        path = new WriteablePath();
        paths = new ArrayList<>();
        paints = new ArrayList<>();

        bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);


        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (Objects.isNull(bitmap)) {
            bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        }

        if (w != oldw || h != oldh) {
            Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas(newBitmap);

            // Draw the old bitmap onto the new one
            newCanvas.drawBitmap(bitmap, 0, 0, null);

            bitmap = newBitmap;
            bitmapCanvas = newCanvas;
        }
    }

    public void setPaths(List<WriteablePath> paths, List<Note.PaintData> paints) {
        this.paths = paths;
        for (WriteablePath writeablePath : this.paths) {
            writeablePath.loadThisPath();
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
                    path = new WriteablePath(); // Create a new path for the next touch event
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