package com.originb.inkwisenote2.modules.handwrittennotes.ui

import android.graphics.*
import com.originb.inkwisenote2.modules.handwrittennotes.data.Stroke
import kotlin.math.max
import kotlin.math.min

/**
 * Utility class to generate thumbnails from DrawingView by trimming empty spaces while respecting minimum size limits
 */
object ThumbnailGenerator {
    // Default thumbnail size
    private const val DEFAULT_THUMBNAIL_WIDTH = 200
    private const val DEFAULT_THUMBNAIL_HEIGHT = 200

    // Minimum crop area size (percentage of original width/height)
    private const val MIN_CROP_PERCENTAGE = 0.3f

    // Padding around content (percentage of crop area)
    private const val PADDING_PERCENTAGE = 0.1f

    @JvmOverloads
    fun generateThumbnail(
        bitmap: Bitmap,
        strokes: MutableList<Stroke>?,
        targetWidth: Int = DEFAULT_THUMBNAIL_WIDTH,
        targetHeight: Int = DEFAULT_THUMBNAIL_HEIGHT
    ): Bitmap? {
        if (strokes == null || strokes.isEmpty()) {
            // If no strokes, return a blank thumbnail
            return createBlankThumbnail(targetWidth, targetHeight)
        }


        // Get original bitmap dimensions
        val originalWidth = bitmap.getWidth()
        val originalHeight = bitmap.getHeight()


        // Find content bounds
        var contentBounds = findContentBounds(strokes)


        // Apply minimum size constraints and padding
        contentBounds = applyConstraints(contentBounds, originalWidth, originalHeight)


        // Create a cropped bitmap of the content area
        val croppedBitmap = createCroppedBitmap(bitmap, strokes, contentBounds)


        // Scale the cropped bitmap to the target thumbnail size
        return scaleBitmap(croppedBitmap, targetWidth, targetHeight)
    }

    /**
     * Finds the bounding box containing all strokes
     *
     * @param strokes List of strokes to analyze
     * @return A RectF representing the bounding box of all content
     */
    private fun findContentBounds(strokes: MutableList<Stroke>): RectF {
        var minX = Float.Companion.MAX_VALUE
        var minY = Float.Companion.MAX_VALUE
        var maxX = 0f
        var maxY = 0f

        var hasPoints = false

        for (stroke in strokes) {
            val points = stroke.getPoints()
            if (points != null && !points.isEmpty()) {
                hasPoints = true
                for (point in points) {
                    val x = point.getX()
                    val y = point.getY()

                    minX = min(minX, x)
                    minY = min(minY, y)
                    maxX = max(maxX, x)
                    maxY = max(maxY, y)
                }
            }
        }


        // If no points were found, return a default centered rectangle
        if (!hasPoints || minX > maxX || minY > maxY) {
            return RectF(0f, 0f, 100f, 100f)
        }

        return RectF(minX, minY, maxX, maxY)
    }

    /**
     * Applies minimum size constraints and padding to the content bounds
     *
     * @param bounds Original content bounds
     * @param fullWidth Full width of the drawing area
     * @param fullHeight Full height of the drawing area
     * @return Adjusted bounds respecting minimum size and padding
     */
    private fun applyConstraints(bounds: RectF, fullWidth: Int, fullHeight: Int): RectF {
        // Calculate content width and height
        val contentWidth = bounds.width()
        val contentHeight = bounds.height()


        // Calculate minimum size based on the original dimensions
        val minWidth = fullWidth * MIN_CROP_PERCENTAGE
        val minHeight = fullHeight * MIN_CROP_PERCENTAGE


        // If content is smaller than minimum, expand it
        if (contentWidth < minWidth) {
            val diff = minWidth - contentWidth
            bounds.left -= diff / 2
            bounds.right += diff / 2
        }

        if (contentHeight < minHeight) {
            val diff = minHeight - contentHeight
            bounds.top -= diff / 2
            bounds.bottom += diff / 2
        }


        // Add padding
        val paddingX = bounds.width() * PADDING_PERCENTAGE
        val paddingY = bounds.height() * PADDING_PERCENTAGE

        bounds.left = max(0f, bounds.left - paddingX)
        bounds.top = max(0f, bounds.top - paddingY)
        bounds.right = min(fullWidth.toFloat(), bounds.right + paddingX)
        bounds.bottom = min(fullHeight.toFloat(), bounds.bottom + paddingY)

        return bounds
    }

    private fun createCroppedBitmap(fullBitmap: Bitmap?, strokes: MutableList<Stroke>, bounds: RectF): Bitmap? {
        val width = bounds.width().toInt()
        val height = bounds.height().toInt()

        if (width <= 0 || height <= 0) {
            return createBlankThumbnail(DEFAULT_THUMBNAIL_WIDTH, DEFAULT_THUMBNAIL_HEIGHT)
        }


        // Create a bitmap for the cropped area
        val croppedBitmap: Bitmap? = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(croppedBitmap!!)


        // Draw with the right offset
        canvas.translate(-bounds.left, -bounds.top)


        // Draw background if needed
        canvas.drawColor(Color.WHITE)


        // If we have a full bitmap, draw the cropped portion
        if (fullBitmap != null && !fullBitmap.isRecycled()) {
            canvas.drawBitmap(fullBitmap, 0f, 0f, null)
        } else {
            // Otherwise, redraw the strokes directly
            drawStrokesOnCanvas(canvas, strokes)
        }

        return croppedBitmap
    }

    /**
     * Draws strokes directly on a canvas
     *
     * @param canvas Canvas to draw on
     * @param strokes Strokes to draw
     */
    private fun drawStrokesOnCanvas(canvas: Canvas, strokes: MutableList<Stroke>) {
        for (stroke in strokes) {
            val points = stroke.getPoints()
            if (points != null && points.size > 0) {
                val paint = Paint()
                paint.setColor(stroke.getColor())
                paint.setStrokeWidth(stroke.getWidth())
                paint.setStyle(Paint.Style.STROKE)
                paint.setStrokeCap(Paint.Cap.ROUND)
                paint.setStrokeJoin(Paint.Join.ROUND)
                paint.setAntiAlias(true)

                val path = Path()
                val firstPoint = points.get(0)
                path.moveTo(firstPoint.getX(), firstPoint.getY())

                for (i in 1..<points.size) {
                    val point = points.get(i)
                    path.lineTo(point.getX(), point.getY())
                }

                canvas.drawPath(path, paint)
            }
        }
    }

    /**
     * Scales a bitmap to the specified dimensions
     *
     * @param source Source bitmap to scale
     * @param targetWidth Target width
     * @param targetHeight Target height
     * @return Scaled bitmap
     */
    private fun scaleBitmap(source: Bitmap?, targetWidth: Int, targetHeight: Int): Bitmap? {
        if (source == null || source.isRecycled()) {
            return createBlankThumbnail(targetWidth, targetHeight)
        }


        // Maintain aspect ratio
        val sourceWidth = source.getWidth().toFloat()
        val sourceHeight = source.getHeight().toFloat()
        val sourceRatio = sourceWidth / sourceHeight
        val targetRatio = targetWidth.toFloat() / targetHeight

        val scaledWidth: Int
        val scaledHeight: Int

        if (sourceRatio > targetRatio) {
            // Source is wider than target
            scaledWidth = targetWidth
            scaledHeight = (targetWidth / sourceRatio).toInt()
        } else {
            // Source is taller than target
            scaledHeight = targetHeight
            scaledWidth = (targetHeight * sourceRatio).toInt()
        }

        val scaledBitmap = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)


        // If the scaled bitmap doesn't match target dimensions exactly, center it in a new bitmap
        if (scaledWidth != targetWidth || scaledHeight != targetHeight) {
            val finalBitmap: Bitmap? = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(finalBitmap!!)
            canvas.drawColor(Color.WHITE)


            // Center the scaled bitmap
            val left = (targetWidth - scaledWidth) / 2f
            val top = (targetHeight - scaledHeight) / 2f
            canvas.drawBitmap(scaledBitmap, left, top, null)


            // Clean up the intermediate bitmap
            scaledBitmap.recycle()
            return finalBitmap
        }

        return scaledBitmap
    }

    /**
     * Creates a blank thumbnail bitmap
     *
     * @param width Width of the thumbnail
     * @param height Height of the thumbnail
     * @return A blank (white) bitmap
     */
    private fun createBlankThumbnail(width: Int, height: Int): Bitmap? {
        val bitmap: Bitmap? = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap!!)
        canvas.drawColor(Color.WHITE)
        return bitmap
    }
}