package com.originb.inkwisenote.ux.activities.smartnotebook;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.data.notedata.PageTemplate;
import com.originb.inkwisenote.data.views.WriteablePath;
import com.originb.inkwisenote.ux.activities.RuledPageBackground;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private RuledPageBackground ruledPageBackground;

    public int currentWidth;
    public int currentHeight;

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

        userDrawingBitmap = getDefaultBitmap();

        userDrwaingCanvas = new Canvas(userDrawingBitmap);
        ruledPageBackground = new RuledPageBackground(ConfigReader.fromContext(context),
                1000, 1000);

        pageTemplateBitmap = ruledPageBackground.drawTemplate();
    }

    public static Bitmap getDefaultBitmap() {
        return Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (oldh * oldw > 100) return; // bitmap has been already initialized
        if (w == 0 || h == 0) return;

        sizeChangeUserDrawingBitmap(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas(newBitmap);

            // Draw the old bitmap onto the new one
            newCanvas.drawBitmap(userDrawingBitmap, 0, 0, null);

            userDrawingBitmap = newBitmap;
            userDrwaingCanvas = newCanvas;
        }

        currentWidth = w;
        currentHeight = h;

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
        int toolType = event.getToolType(0);

        if (toolType != MotionEvent.TOOL_TYPE_STYLUS
                && toolType != MotionEvent.TOOL_TYPE_FINGER
        ) {
            // Process only stylus input
            return false;
        }

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