package com.originb.inkwisenote.ux.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.data.notedata.PageTemplate;
import com.originb.inkwisenote.data.views.WriteablePath;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.util.SparseArray;

@Getter
public class PalmRejectionDrawingView extends View {
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

    private RuledPageBackground ruledPageBackground;

    private Paint paint;
    private WriteablePath path;
    private List<WriteablePath> paths;
    private List<Paint> paints;

    private static final int MAX_SIMULTANEOUS_TOUCHES = 2;
    private static final float PALM_PRESSURE_THRESHOLD = 0.3f;
    private static final float TOUCH_SIZE_THRESHOLD = 0.15f;
    private static final int TOUCH_SLOP = 8;

    private SparseArray<TouchPoint> activePointers;
    private boolean isPalmRejectionEnabled = true;

    public PalmRejectionDrawingView(Context context, AttributeSet attrs) {
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
        activePointers = new SparseArray<>();

        userDrawingBitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);

        userDrwaingCanvas = new Canvas(userDrawingBitmap);
        ruledPageBackground = new RuledPageBackground(ConfigReader.fromContext(context),
                1000, 1000);

        pageTemplateBitmap = ruledPageBackground.drawTemplate();
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

        ruledPageBackground.onSizeChanged(w, h, oldw, oldh);
        pageTemplateBitmap = ruledPageBackground.drawTemplate();
    }

    private void sizeChangeUserDrawingBitmap(int w, int h, int oldw, int oldh) {
        if (Objects.isNull(userDrawingBitmap)) {
            userDrawingBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
    }

    public Bitmap getNewBitmap() {
        return Bitmap.createBitmap(userDrawingBitmap.getWidth(),
                userDrawingBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
    }

    public Bitmap getBitmap() {
        return userDrawingBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.userDrawingBitmap = bitmap;
        userDrwaingCanvas = new Canvas(userDrawingBitmap);
        invalidate();
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
        if (!isPalmRejectionEnabled) {
            return handleDrawing(event);
        }

        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        int toolType = event.getToolType(pointerIndex);

        // Always accept stylus input
        if (toolType == MotionEvent.TOOL_TYPE_STYLUS) {
            return handleDrawing(event);
        }

        // For finger touches, apply palm rejection logic
        if (toolType == MotionEvent.TOOL_TYPE_FINGER) {
            TouchPoint touch = new TouchPoint();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (activePointers.size() >= MAX_SIMULTANEOUS_TOUCHES) {
                        return false;
                    }

                    // Check pressure and touch size
                    float pressure = event.getPressure(pointerIndex);
                    float touchSize = event.getSize(pointerIndex);

                    if (pressure > PALM_PRESSURE_THRESHOLD || touchSize > TOUCH_SIZE_THRESHOLD) {
                        return false;
                    }

                    touch.x = event.getX(pointerIndex);
                    touch.y = event.getY(pointerIndex);
                    activePointers.put(pointerId, touch);
                    return handleDrawing(event);

                case MotionEvent.ACTION_MOVE:
                    touch = activePointers.get(pointerId);
                    if (touch != null) {
                        float deltaX = Math.abs(event.getX(pointerIndex) - touch.x);
                        float deltaY = Math.abs(event.getY(pointerIndex) - touch.y);

                        if (deltaX > TOUCH_SLOP * 3 || deltaY > TOUCH_SLOP * 3) {
                            // Erratic movement, likely palm
                            activePointers.remove(pointerId);
                            return false;
                        }

                        touch.x = event.getX(pointerIndex);
                        touch.y = event.getY(pointerIndex);
                        return handleDrawing(event);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    activePointers.remove(pointerId);
                    return handleDrawing(event);

                case MotionEvent.ACTION_CANCEL:
                    activePointers.clear();
                    break;
            }
        }

        return false;
    }

    private boolean handleDrawing(MotionEvent event) {
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
                userDrwaingCanvas.drawPath(path, paint);
                path = new WriteablePath();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private static class TouchPoint {
        float x, y;
    }

    public PageTemplate getNewPageTemplate() {
        return ruledPageBackground.getPageTemplate();
    }

    public PageTemplate getPageTemplate() {
        return ruledPageBackground.getPageTemplate();
    }

    public void setPageTemplate(PageTemplate data) {
        if (Objects.isNull(data)) {
            return;
        }
        ruledPageBackground.setPageTemplate(data);
    }
}