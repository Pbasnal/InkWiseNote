package com.originb.inkwisenote.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.originb.inkwisenote.data.config.PageTemplate;
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
    private Bitmap userDrawingBitmap;
    private Bitmap pageTemplateBitmap;
    private Canvas userDrwaingCanvas;

    private BasicPageTemplate basicPageTemplate;

    private Paint paint;
    private WriteablePath path;
    private List<WriteablePath> paths;
    private List<Paint> paints;

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

        userDrawingBitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);

        userDrwaingCanvas = new Canvas(userDrawingBitmap);
        basicPageTemplate = new BasicPageTemplate(1000, 1000);

        pageTemplateBitmap = basicPageTemplate.drawTemplate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        sizeChangeUserDrawingBitmap(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas(newBitmap);

            // Draw the old bitmap onto the new one
            newCanvas.drawBitmap(userDrawingBitmap, 0, 0, null);

            userDrawingBitmap = newBitmap;
            userDrwaingCanvas = newCanvas;
        }

        basicPageTemplate.onSizeChanged(w, h, oldw, oldh);
        pageTemplateBitmap = basicPageTemplate.drawTemplate();
    }

    private void sizeChangeUserDrawingBitmap(int w, int h, int oldw, int oldh) {
        if (Objects.isNull(userDrawingBitmap)) {
            userDrawingBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
    }

    public Bitmap getBitmap() {
        return userDrawingBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.userDrawingBitmap = bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        // Draw the cached bitmap
        canvas.drawBitmap(pageTemplateBitmap, 0, 0, null);
        canvas.drawBitmap(userDrawingBitmap, 0, 0, null);
        canvas.drawPath(path, paint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        float x = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                userDrwaingCanvas.drawPath(path, paint); // Draw the current path onto the bitmap
                path = new WriteablePath(); // Create a new path for the next touch event
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public PageTemplate getPageTemplate() {
        return basicPageTemplate.getPageTemplate();
    }

    public void setPageTemplate(PageTemplate data) {
        if (Objects.isNull(data)) {
            return;
        }
        basicPageTemplate.setPageTemplate(data);
    }
}