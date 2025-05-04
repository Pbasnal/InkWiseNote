package com.originb.inkwisenote2.modules.handwrittennotes.ui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.originb.inkwisenote2.config.ConfigReader;
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate;
import com.originb.inkwisenote2.modules.handwrittennotes.RuledPageBackground;
import com.originb.inkwisenote2.modules.handwrittennotes.data.Stroke;
import com.originb.inkwisenote2.modules.handwrittennotes.data.StrokePoint;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Iterator;
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

    // Collection of strokes for markdown export
    private List<Stroke> strokes;
    private Stroke currentStroke;
    private float lastPressure = 1.0f;

    // Eraser mode flag and properties
    private boolean eraserMode = false;
    private float eraserSize = 30f;
    private Paint eraserPaint;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize paint with pencil-like properties
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f); // Thinner base stroke
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND); // Smooth line joins
//        paint.setAlpha(200); // Slight transparency

        // Initialize eraser paint
        eraserPaint = new Paint();
        eraserPaint.setColor(Color.TRANSPARENT);
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setStrokeWidth(eraserSize);
        eraserPaint.setAntiAlias(true);
        eraserPaint.setStrokeCap(Paint.Cap.ROUND);
        eraserPaint.setStrokeJoin(Paint.Join.ROUND);

        // Add shader for texture
        paint.setShader(createPencilShader());

        // Enable dithering for smoother gradients
        paint.setDither(true);

        path = new WriteablePath();
        paths = new ArrayList<>();
        paints = new ArrayList<>();

        // Initialize strokes collection
        strokes = new ArrayList<>();

        userDrawingBitmap = getDefaultBitmap();

        userDrwaingCanvas = new Canvas(userDrawingBitmap);
        ruledPageBackground = new RuledPageBackground(getContext(), ConfigReader.fromContext(context),
                1000, 1000);

        pageTemplateBitmap = ruledPageBackground.drawTemplate();

        DrawingView thisView = this;
        ViewTreeObserver vto = this.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                currentWidth = thisView.getWidth();
                currentHeight = thisView.getHeight();

                // Now you can use width/height

                // Remove listener if you only need it once
                redrawStrokesOnBitmap();
                ruledPageBackground.onSizeChanged(currentWidth, currentHeight, 0, 0);
                pageTemplateBitmap = ruledPageBackground.drawTemplate();
                thisView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    private Shader createPencilShader() {
        // Create a subtle texture pattern
        Bitmap textureBitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
        Canvas textureCanvas = new Canvas(textureBitmap);
        Paint texturePaint = new Paint();
        texturePaint.setColor(Color.BLACK);

        // Create a subtle noise pattern
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                if ((x + y) % 2 == 0) {
                    textureBitmap.setPixel(x, y, Color.argb(200, 0, 0, 0));
                } else {
                    textureBitmap.setPixel(x, y, Color.argb(180, 0, 0, 0));
                }
            }
        }

        return new BitmapShader(textureBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
    }

    public static Bitmap getDefaultBitmap() {
        return Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
    }

    public Bitmap getNewBitmap() {
        if(currentWidth >0 && currentHeight > 0) {
            return Bitmap.createBitmap(currentWidth,
                    currentHeight,
                    Bitmap.Config.ARGB_8888);
        }

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
                && toolType != MotionEvent.TOOL_TYPE_FINGER) {
            return false;
        }

        float y = event.getY();
        float x = event.getX();
        float pressure = event.getPressure();
        // If pressure reading isn't supported, use 1.0f
        if (pressure <= 0) {
            pressure = lastPressure;
        } else {
            lastPressure = pressure;
        }

        if (eraserMode) {
            return handleEraserTouch(event, x, y);
        } else {
            return handleDrawingTouch(event, x, y, pressure);
        }
    }

    private boolean handleDrawingTouch(MotionEvent event, float x, float y, float pressure) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                // Start a new stroke for markdown export
                currentStroke = new Stroke(paint.getColor(), paint.getStrokeWidth());
                currentStroke.addPoint(x, y, pressure);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                // Add point to current stroke
                if (currentStroke != null) {
                    currentStroke.addPoint(x, y, pressure);
                }
                break;
            case MotionEvent.ACTION_UP:
                userDrwaingCanvas.drawPath(path, paint); // Draw the current path onto the bitmap
                path = new WriteablePath(); // Create a new path for the next touch event
                // Finalize the current stroke and add to collection
                if (currentStroke != null) {
                    strokes.add(currentStroke);
                    currentStroke = null;
                }
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private boolean handleEraserTouch(MotionEvent event, float x, float y) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                eraseStrokesAt(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                eraseStrokesAt(x, y);
                break;
            case MotionEvent.ACTION_UP:
                // Nothing special to do on up for eraser
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private void eraseStrokesAt(float x, float y) {
        boolean strokesErased = false;

        // Create a touch area for eraser
        RectF eraserRect = new RectF(
                x - eraserSize / 2,
                y - eraserSize / 2,
                x + eraserSize / 2,
                y + eraserSize / 2
        );

        // Iterate through strokes and remove those that intersect with eraser
        Iterator<Stroke> strokeIterator = strokes.iterator();
        while (strokeIterator.hasNext()) {
            Stroke stroke = strokeIterator.next();
            List<StrokePoint> points = stroke.getPoints();

            // Check if any point in the stroke is within eraser area
            for (StrokePoint point : points) {
                if (eraserRect.contains(point.getX(), point.getY())) {
                    strokeIterator.remove();
                    strokesErased = true;
                    break;
                }
            }
        }

        // If we erased any strokes, redraw the bitmap
        if (strokesErased) {
            redrawStrokesOnBitmap();
        }
    }

    /**
     * Set eraser mode on or off
     *
     * @param enabled true to enable eraser mode, false for drawing mode
     */
    public void setEraserMode(boolean enabled) {
        this.eraserMode = enabled;
        // Clear current path when switching modes
        path = new WriteablePath();
        currentStroke = null;
        invalidate();
    }

    /**
     * Check if eraser mode is currently active
     *
     * @return true if in eraser mode, false if in drawing mode
     */
    public boolean isEraserMode() {
        return eraserMode;
    }

    /**
     * Set the eraser size
     *
     * @param size diameter of the eraser in pixels
     */
    public void setEraserSize(float size) {
        this.eraserSize = size;
        eraserPaint.setStrokeWidth(size);
    }

    /**
     * Gets all strokes recorded for markdown export
     *
     * @return List of strokes
     */
    public List<Stroke> getStrokes() {
        return strokes;
    }

    /**
     * Clears all strokes
     */
    public void clearStrokes() {
        strokes.clear();
        invalidate();
    }

    /**
     * Sets strokes list from external source (when loading from markdown)
     *
     * @param loadedStrokes List of strokes to set
     */
    public void setStrokes(List<Stroke> loadedStrokes) {
        if (loadedStrokes != null) {
            strokes = new ArrayList<>(loadedStrokes);
            redrawStrokesOnBitmap();
            invalidate();
        }
    }

    /**
     * Redraws all strokes on the bitmap
     */
    private void redrawStrokesOnBitmap() {
        // Clear current bitmap
        userDrawingBitmap = getNewBitmap();
        userDrwaingCanvas = new Canvas(userDrawingBitmap);

        // Redraw each stroke
        for (Stroke stroke : strokes) {
            Paint strokePaint = new Paint(paint);
            strokePaint.setColor(stroke.getColor());
            strokePaint.setStrokeWidth(stroke.getWidth());

            Path strokePath = new Path();
            List<StrokePoint> points = stroke.getPoints();

            if (points.size() > 0) {
                StrokePoint firstPoint = points.get(0);
                strokePath.moveTo(firstPoint.getX(), firstPoint.getY());

                for (int i = 1; i < points.size(); i++) {
                    StrokePoint point = points.get(i);
                    strokePath.lineTo(point.getX(), point.getY());
                }

                userDrwaingCanvas.drawPath(strokePath, strokePaint);
            }
        }
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