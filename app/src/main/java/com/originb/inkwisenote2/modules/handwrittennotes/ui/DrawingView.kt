package com.originb.inkwisenote2.modules.handwrittennotes.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.originb.inkwisenote2.config.ConfigReader
import com.originb.inkwisenote2.modules.handwrittennotes.RuledPageBackground
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate
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

    // Initialize paint with pencil-like properties
    private val paint = Paint()
    private var path: WriteablePath
    private val paths: List<WriteablePath>
    private val paints: List<Paint>

    init {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f // Thinner base stroke
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND // Smooth line joins


        //        paint.setAlpha(200); // Slight transparency


        // Add shader for texture
        paint.setShader(createPencilShader())

        // Enable dithering for smoother gradients
        paint.isDither = true

        path = WriteablePath()
        paths = ArrayList()
        paints = ArrayList()

        userDrawingBitmap = defaultBitmap

        userDrwaingCanvas = Canvas(userDrawingBitmap)
        ruledPageBackground = RuledPageBackground(
            getContext(), ConfigReader.Companion.fromContext(context),
            1000, 1000
        )

        pageTemplateBitmap = ruledPageBackground.drawTemplate()
    }

    private fun createPencilShader(): Shader {
        // Create a subtle texture pattern
        val textureBitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        val textureCanvas = Canvas(textureBitmap)
        val texturePaint = Paint()
        texturePaint.color = Color.BLACK

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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (oldh * oldw > 100) return  // bitmap has been already initialized

        if (w == 0 || h == 0) return

        sizeChangeUserDrawingBitmap(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {
            val newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val newCanvas = Canvas(newBitmap)

            // Draw the old bitmap onto the new one
            newCanvas.drawBitmap(userDrawingBitmap, 0f, 0f, null)

            userDrawingBitmap = newBitmap
            userDrwaingCanvas = newCanvas
        }

        currentWidth = w
        currentHeight = h

        ruledPageBackground.onSizeChanged(w, h, oldw, oldh)
        pageTemplateBitmap = ruledPageBackground.drawTemplate()
    }

    private fun sizeChangeUserDrawingBitmap(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (Objects.isNull(userDrawingBitmap)) {
            userDrawingBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        }
    }

    val newBitmap: Bitmap
        get() = Bitmap.createBitmap(
            userDrawingBitmap.width,
            userDrawingBitmap.height,
            Bitmap.Config.ARGB_8888
        )

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

        val y = event.y
        val x = event.x

        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(x, y)
            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
            MotionEvent.ACTION_UP -> {
                userDrwaingCanvas.drawPath(path, paint) // Draw the current path onto the bitmap
                path = WriteablePath() // Create a new path for the next touch event
            }

            else -> return false
        }
        invalidate()
        return true
    }

    val newPageTemplate: PageTemplate
        get() = ruledPageBackground.pageTemplate

    var pageTemplate: PageTemplate?
        get() = ruledPageBackground.pageTemplate
        set(data) {
            if (Objects.isNull(data)) {
                return
            }
            ruledPageBackground.pageTemplate = data
        }

    companion object {
        val defaultBitmap: Bitmap
            get() = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
    }
}