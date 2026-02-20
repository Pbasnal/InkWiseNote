package com.originb.inkwisenote2.modules.handwrittennotes.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.originb.inkwisenote2.config.CanvasSize
import com.originb.inkwisenote2.config.ConfigReader
import com.originb.inkwisenote2.config.ConfigReader.Companion.fromContext
import com.originb.inkwisenote2.config.ConfigReader.Companion.getInstance
import com.originb.inkwisenote2.modules.handwrittennotes.RuledPageBackground
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate
import com.originb.inkwisenote2.modules.handwrittennotes.data.Stroke
import lombok.Getter
import java.util.*

@Getter
class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    /*
     * The bitmap is used to cache the drawing so that it doesn't have to be redrawn every time onDraw is called.
     * The bitmapCanvas is used to draw the paths onto the bitmap.
     *
     * user draws a path on the screen --> updates path --> bitmapCanvas updates bitmap
     * onDraw is called --> canvas draws updated bitmap
     *                  --> canvas draws updated path. [this is the path that user is currently drawing]
     * */
    private var userDrawingBitmap: Bitmap
    private var pageTemplateBitmap: Bitmap?
    private var userDrwaingCanvas: Canvas

    private val ruledPageBackground: RuledPageBackground

    var currentWidth: Int = 0
    var currentHeight: Int = 0

    private val paint: Paint
    private var path: WriteablePath
    private val paths: MutableList<WriteablePath?>?
    private val paints: MutableList<Paint?>?

    /**
     * Gets all strokes recorded for markdown export
     *
     * @return List of strokes
     */
    // Collection of strokes for markdown export
    var strokes: MutableList<Stroke>
        private set
    private var currentStroke: Stroke? = null
    private var lastPressure = 1.0f

    // Eraser mode flag and properties
    private var eraserMode = false
    private var eraserSize = 30f
    private val eraserPaint: Paint

    private val configReader: ConfigReader?
    private val canvasSize: CanvasSize?

    init {
        configReader = getInstance()
        canvasSize = configReader.getAppConfig().getCanvasSizes().get(0)

        // Initialize paint with pencil-like properties
        paint = Paint()
        paint.setColor(Color.BLACK)
        paint.setStyle(Paint.Style.STROKE)
        paint.setStrokeWidth(5f) // Thinner base stroke
        paint.setAntiAlias(true)
        paint.setStrokeCap(Paint.Cap.ROUND)
        paint.setStrokeJoin(Paint.Join.ROUND) // Smooth line joins

        //        paint.setAlpha(200); // Slight transparency

        // Initialize eraser paint
        eraserPaint = Paint()
        eraserPaint.setColor(Color.TRANSPARENT)
        eraserPaint.setStyle(Paint.Style.STROKE)
        eraserPaint.setStrokeWidth(eraserSize)
        eraserPaint.setAntiAlias(true)
        eraserPaint.setStrokeCap(Paint.Cap.ROUND)
        eraserPaint.setStrokeJoin(Paint.Join.ROUND)

        // Add shader for texture
        paint.setShader(createPencilShader())

        // Enable dithering for smoother gradients
        paint.setDither(true)

        path = WriteablePath()
        paths = ArrayList<WriteablePath?>()
        paints = ArrayList<Paint?>()

        // Initialize strokes collection
        strokes = ArrayList<Stroke>()

        userDrawingBitmap = defaultBitmap

        userDrwaingCanvas = Canvas(userDrawingBitmap)
        ruledPageBackground = RuledPageBackground(
            getContext(), fromContext(context),
            1000, 1000
        )

        pageTemplateBitmap = ruledPageBackground.drawTemplate()

        val thisView = this
        val vto = this.getViewTreeObserver()
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val newWidth = thisView.getWidth()
                val newHeight = thisView.getHeight()

                if (currentWidth > newWidth || currentHeight > newHeight) {
                    return
                }
                currentWidth = thisView.getWidth()
                currentHeight = thisView.getHeight()

                // Now you can use width/height

                // Remove listener if you only need it once
                redrawStrokesOnBitmap()
                ruledPageBackground.onSizeChanged(currentWidth, currentHeight, 0, 0)
                pageTemplateBitmap = ruledPageBackground.drawTemplate()
                //                thisView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        })
    }

    private fun createPencilShader(): Shader {
        // Create a subtle texture pattern
        val textureBitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        val texturePaint = Paint()
        texturePaint.setColor(Color.BLACK)

        // Create a subtle noise pattern
        for (x in 0..3) {
            for (y in 0..3) {
                if ((x + y) % 2 == 0) {
                    textureBitmap.setPixel(x, y, Color.argb(200, 0, 0, 0))
                } else {
                    textureBitmap.setPixel(x, y, Color.argb(180, 0, 0, 0))
                }
            }
        }

        return BitmapShader(textureBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    val newBitmap: Bitmap
        get() {
            if (currentWidth > 0 && currentHeight > 0) {
                return Bitmap.createBitmap(
                    currentWidth,
                    currentHeight,
                    Bitmap.Config.ARGB_8888
                )
            }

            return Bitmap.createBitmap(
                userDrawingBitmap.getWidth(),
                userDrawingBitmap.getHeight(),
                Bitmap.Config.ARGB_8888
            )
        }

    var bitmap: Bitmap
        get() = userDrawingBitmap
        set(bitmap) {
            this.userDrawingBitmap = bitmap
            userDrwaingCanvas = Canvas(userDrawingBitmap)
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        // Draw the cached bitmap
        canvas.drawBitmap(pageTemplateBitmap!!, 0f, 0f, null)
        canvas.drawBitmap(userDrawingBitmap, 0f, 0f, null)
        canvas.drawPath(path, paint)

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val toolType = event.getToolType(0)

        if (toolType != MotionEvent.TOOL_TYPE_STYLUS
            && toolType != MotionEvent.TOOL_TYPE_FINGER
        ) {
            return false
        }

        val y = event.getY()
        val x = event.getX()
        var pressure = event.getPressure()
        // If pressure reading isn't supported, use 1.0f
        if (pressure <= 0) {
            pressure = lastPressure
        } else {
            lastPressure = pressure
        }

        if (eraserMode) {
            return handleEraserTouch(event, x, y)
        } else {
            return handleDrawingTouch(event, x, y, pressure)
        }
    }

    private fun handleDrawingTouch(event: MotionEvent, x: Float, y: Float, pressure: Float): Boolean {
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                // Start a new stroke for markdown export
                currentStroke = Stroke(paint.getColor(), paint.getStrokeWidth())
                currentStroke!!.addPoint(x, y, pressure, event.getEventTime())
            }

            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                // Add point to current stroke
                if (currentStroke != null) {
                    currentStroke!!.addPoint(x, y, pressure, event.getEventTime())
                }
            }

            MotionEvent.ACTION_UP -> {
                userDrwaingCanvas.drawPath(path, paint) // Draw the current path onto the bitmap
                path = WriteablePath() // Create a new path for the next touch event
                // Finalize the current stroke and add to collection
                if (currentStroke != null) {
                    strokes.add(currentStroke!!)
                    currentStroke = null
                }
            }

            else -> return false
        }

        invalidate()
        return true
    }

    private fun handleEraserTouch(event: MotionEvent, x: Float, y: Float): Boolean {
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> eraseStrokesAt(x, y)
            MotionEvent.ACTION_MOVE -> eraseStrokesAt(x, y)
            MotionEvent.ACTION_UP -> {}
            else -> return false
        }

        invalidate()
        return true
    }

    private fun eraseStrokesAt(x: Float, y: Float) {
        var strokesErased = false

        // Create a touch area for eraser
        val eraserRect = RectF(
            x - eraserSize / 2,
            y - eraserSize / 2,
            x + eraserSize / 2,
            y + eraserSize / 2
        )

        // Iterate through strokes and remove those that intersect with eraser
        val strokeIterator = strokes.iterator()
        while (strokeIterator.hasNext()) {
            val stroke = strokeIterator.next()
            val points = stroke.getPoints()

            // Check if any point in the stroke is within eraser area
            for (point in points) {
                if (eraserRect.contains(point.getX(), point.getY())) {
                    strokeIterator.remove()
                    strokesErased = true
                    break
                }
            }
        }

        // If we erased any strokes, redraw the bitmap
        if (strokesErased) {
            redrawStrokesOnBitmap()
        }
    }

    /**
     * Set eraser mode on or off
     *
     * @param enabled true to enable eraser mode, false for drawing mode
     */
    fun setEraserMode(enabled: Boolean) {
        this.eraserMode = enabled
        // Clear current path when switching modes
        path = WriteablePath()
        currentStroke = null
        invalidate()
    }

    /**
     * Check if eraser mode is currently active
     *
     * @return true if in eraser mode, false if in drawing mode
     */
    fun isEraserMode(): Boolean {
        return eraserMode
    }

    /**
     * Set the eraser size
     *
     * @param size diameter of the eraser in pixels
     */
    fun setEraserSize(size: Float) {
        this.eraserSize = size
        eraserPaint.setStrokeWidth(size)
    }

    /**
     * Clears all strokes
     */
    fun clearStrokes() {
        strokes.clear()
        invalidate()
    }

    /**
     * Sets strokes list from external source (when loading from markdown)
     *
     * @param loadedStrokes List of strokes to set
     */
    fun setStrokes(loadedStrokes: MutableList<Stroke?>?) {
        if (loadedStrokes != null) {
            strokes = ArrayList<Stroke>(loadedStrokes)
            redrawStrokesOnBitmap()
            invalidate()
        }
    }

    /**
     * Redraws all strokes on the bitmap
     */
    private fun redrawStrokesOnBitmap() {
        // Clear current bitmap
        userDrawingBitmap = this.newBitmap
        userDrwaingCanvas = Canvas(userDrawingBitmap)

        // Redraw each stroke
        for (stroke in strokes) {
            val strokePaint = Paint(paint)
            strokePaint.setColor(stroke.getColor())
            strokePaint.setStrokeWidth(stroke.getWidth())

            val strokePath = Path()
            val points = stroke.getPoints()

            if (points.size > 0) {
                val firstPoint = points.get(0)
                strokePath.moveTo(firstPoint.getX(), firstPoint.getY())

                for (i in 1..<points.size) {
                    val point = points.get(i)
                    strokePath.lineTo(point.getX(), point.getY())
                }

                userDrwaingCanvas.drawPath(strokePath, strokePaint)
            }
        }
    }

    val newPageTemplate: PageTemplate?
        get() = ruledPageBackground.getPageTemplate()

    var pageTemplate: PageTemplate?
        get() = ruledPageBackground.getPageTemplate()
        set(data) {
            if (Objects.isNull(data)) {
                return
            }
            ruledPageBackground.setPageTemplate(data)
        }

    companion object {
        val defaultBitmap: Bitmap
            get() = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
    }
}