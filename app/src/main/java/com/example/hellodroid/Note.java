package com.example.hellodroid;

import android.graphics.Paint;
import android.graphics.Path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Note implements Serializable {
    private static final long serialVersionUID = 1L;

    // List to store drawing paths
    private List<MyPath> paths;

    // List to store corresponding paint objects for each path
    private List<PaintData> paints;



    public Note() {
        paths = new ArrayList<>();
        paints = new ArrayList<>();
    }

    // Add a path with its corresponding paint
    public void setNoteData(List<MyPath> paths, List<Paint> paints) {
        this.paths = paths;
        this.paints = paints.stream().map(PaintData::new).collect(Collectors.toList());
    }

    // Get paths
    public List<MyPath> getPaths() {
        return paths;
    }

    // Get paints
    public List<PaintData> getPaints() {
        return paints;
    }

    // Inner class to store paint properties since Paint itself is not Serializable
    public static class PaintData implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int color;
        private final float strokeWidth;
        private final Paint.Style style;
        private final Paint.Cap strokeCap;

        public PaintData(Paint paint) {
            this.color = paint.getColor();
            this.strokeWidth = paint.getStrokeWidth();
            this.style = paint.getStyle();
            this.strokeCap = paint.getStrokeCap();
        }

        // Restore Paint object from PaintData
        public Paint toPaint() {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setStrokeWidth(strokeWidth);
            paint.setStyle(style);
            paint.setStrokeCap(strokeCap);
            paint.setAntiAlias(true);
            return paint;
        }
    }
}