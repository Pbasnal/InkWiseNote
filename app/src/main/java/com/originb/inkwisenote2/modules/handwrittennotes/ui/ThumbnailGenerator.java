package com.originb.inkwisenote2.modules.handwrittennotes.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.originb.inkwisenote2.modules.handwrittennotes.data.Stroke;
import com.originb.inkwisenote2.modules.handwrittennotes.data.StrokePoint;

import java.util.List;

/**
 * Utility class to generate thumbnails from DrawingView by trimming empty spaces while respecting minimum size limits
 */
public class ThumbnailGenerator {
    
    // Default thumbnail size
    private static final int DEFAULT_THUMBNAIL_WIDTH = 200;
    private static final int DEFAULT_THUMBNAIL_HEIGHT = 200;
    
    // Minimum crop area size (percentage of original width/height)
    private static final float MIN_CROP_PERCENTAGE = 0.3f;
    
    // Padding around content (percentage of crop area)
    private static final float PADDING_PERCENTAGE = 0.1f;

    public static Bitmap generateThumbnail(Bitmap bitmap, List<Stroke> strokes) {
        return generateThumbnail(bitmap, strokes, DEFAULT_THUMBNAIL_WIDTH, DEFAULT_THUMBNAIL_HEIGHT);
    }

    public static Bitmap generateThumbnail(Bitmap bitmap, List<Stroke> strokes, int targetWidth, int targetHeight) {
        if (strokes == null || strokes.isEmpty()) {
            // If no strokes, return a blank thumbnail
            return createBlankThumbnail(targetWidth, targetHeight);
        }
        
        // Get original bitmap dimensions
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        
        // Find content bounds
        RectF contentBounds = findContentBounds(strokes);
        
        // Apply minimum size constraints and padding
        contentBounds = applyConstraints(contentBounds, originalWidth, originalHeight);
        
        // Create a cropped bitmap of the content area
        Bitmap croppedBitmap = createCroppedBitmap(bitmap, strokes, contentBounds);
        
        // Scale the cropped bitmap to the target thumbnail size
        return scaleBitmap(croppedBitmap, targetWidth, targetHeight);
    }
    
    /**
     * Finds the bounding box containing all strokes
     * 
     * @param strokes List of strokes to analyze
     * @return A RectF representing the bounding box of all content
     */
    private static RectF findContentBounds(List<Stroke> strokes) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = 0;
        float maxY = 0;
        
        boolean hasPoints = false;
        
        for (Stroke stroke : strokes) {
            List<StrokePoint> points = stroke.getPoints();
            if (points != null && !points.isEmpty()) {
                hasPoints = true;
                for (StrokePoint point : points) {
                    float x = point.getX();
                    float y = point.getY();
                    
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        
        // If no points were found, return a default centered rectangle
        if (!hasPoints || minX > maxX || minY > maxY) {
            return new RectF(0, 0, 100, 100);
        }
        
        return new RectF(minX, minY, maxX, maxY);
    }
    
    /**
     * Applies minimum size constraints and padding to the content bounds
     * 
     * @param bounds Original content bounds
     * @param fullWidth Full width of the drawing area
     * @param fullHeight Full height of the drawing area
     * @return Adjusted bounds respecting minimum size and padding
     */
    private static RectF applyConstraints(RectF bounds, int fullWidth, int fullHeight) {
        // Calculate content width and height
        float contentWidth = bounds.width();
        float contentHeight = bounds.height();
        
        // Calculate minimum size based on the original dimensions
        float minWidth = fullWidth * MIN_CROP_PERCENTAGE;
        float minHeight = fullHeight * MIN_CROP_PERCENTAGE;
        
        // If content is smaller than minimum, expand it
        if (contentWidth < minWidth) {
            float diff = minWidth - contentWidth;
            bounds.left -= diff / 2;
            bounds.right += diff / 2;
        }
        
        if (contentHeight < minHeight) {
            float diff = minHeight - contentHeight;
            bounds.top -= diff / 2;
            bounds.bottom += diff / 2;
        }
        
        // Add padding
        float paddingX = bounds.width() * PADDING_PERCENTAGE;
        float paddingY = bounds.height() * PADDING_PERCENTAGE;
        
        bounds.left = Math.max(0, bounds.left - paddingX);
        bounds.top = Math.max(0, bounds.top - paddingY);
        bounds.right = Math.min(fullWidth, bounds.right + paddingX);
        bounds.bottom = Math.min(fullHeight, bounds.bottom + paddingY);
        
        return bounds;
    }

    private static Bitmap createCroppedBitmap(Bitmap fullBitmap, List<Stroke> strokes, RectF bounds) {
        int width = (int) bounds.width();
        int height = (int) bounds.height();
        
        if (width <= 0 || height <= 0) {
            return createBlankThumbnail(DEFAULT_THUMBNAIL_WIDTH, DEFAULT_THUMBNAIL_HEIGHT);
        }
        
        // Create a bitmap for the cropped area
        Bitmap croppedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedBitmap);
        
        // Draw with the right offset
        canvas.translate(-bounds.left, -bounds.top);
        
        // Draw background if needed
        canvas.drawColor(Color.WHITE);
        
        // If we have a full bitmap, draw the cropped portion
        if (fullBitmap != null && !fullBitmap.isRecycled()) {
            canvas.drawBitmap(fullBitmap, 0, 0, null);
        } else {
            // Otherwise, redraw the strokes directly
            drawStrokesOnCanvas(canvas, strokes);
        }
        
        return croppedBitmap;
    }
    
    /**
     * Draws strokes directly on a canvas
     * 
     * @param canvas Canvas to draw on
     * @param strokes Strokes to draw
     */
    private static void drawStrokesOnCanvas(Canvas canvas, List<Stroke> strokes) {
        for (Stroke stroke : strokes) {
            List<StrokePoint> points = stroke.getPoints();
            if (points != null && points.size() > 0) {
                Paint paint = new Paint();
                paint.setColor(stroke.getColor());
                paint.setStrokeWidth(stroke.getWidth());
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setAntiAlias(true);
                
                Path path = new Path();
                StrokePoint firstPoint = points.get(0);
                path.moveTo(firstPoint.getX(), firstPoint.getY());
                
                for (int i = 1; i < points.size(); i++) {
                    StrokePoint point = points.get(i);
                    path.lineTo(point.getX(), point.getY());
                }
                
                canvas.drawPath(path, paint);
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
    private static Bitmap scaleBitmap(Bitmap source, int targetWidth, int targetHeight) {
        if (source == null || source.isRecycled()) {
            return createBlankThumbnail(targetWidth, targetHeight);
        }
        
        // Maintain aspect ratio
        float sourceWidth = source.getWidth();
        float sourceHeight = source.getHeight();
        float sourceRatio = sourceWidth / sourceHeight;
        float targetRatio = (float) targetWidth / targetHeight;
        
        int scaledWidth, scaledHeight;
        
        if (sourceRatio > targetRatio) {
            // Source is wider than target
            scaledWidth = targetWidth;
            scaledHeight = (int) (targetWidth / sourceRatio);
        } else {
            // Source is taller than target
            scaledHeight = targetHeight;
            scaledWidth = (int) (targetHeight * sourceRatio);
        }
        
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true);
        
        // If the scaled bitmap doesn't match target dimensions exactly, center it in a new bitmap
        if (scaledWidth != targetWidth || scaledHeight != targetHeight) {
            Bitmap finalBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(finalBitmap);
            canvas.drawColor(Color.WHITE);
            
            // Center the scaled bitmap
            float left = (targetWidth - scaledWidth) / 2f;
            float top = (targetHeight - scaledHeight) / 2f;
            canvas.drawBitmap(scaledBitmap, left, top, null);
            
            // Clean up the intermediate bitmap
            scaledBitmap.recycle();
            return finalBitmap;
        }
        
        return scaledBitmap;
    }
    
    /**
     * Creates a blank thumbnail bitmap
     * 
     * @param width Width of the thumbnail
     * @param height Height of the thumbnail
     * @return A blank (white) bitmap
     */
    private static Bitmap createBlankThumbnail(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        return bitmap;
    }
} 