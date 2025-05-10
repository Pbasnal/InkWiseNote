package com.originb.inkwisenote2.modules.handwrittennotes.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single stroke in a handwritten note
 */
public class Stroke implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int color;
    private float width;
    private List<StrokePoint> points;
    
    public Stroke() {
        this.points = new ArrayList<>();
    }
    
    public Stroke(int color, float width) {
        this.color = color;
        this.width = width;
        this.points = new ArrayList<>();
    }
    
    public int getColor() {
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
    
    public float getWidth() {
        return width;
    }
    
    public void setWidth(float width) {
        this.width = width;
    }
    
    public List<StrokePoint> getPoints() {
        return points;
    }
    
    public void setPoints(List<StrokePoint> points) {
        this.points = points;
    }
    
    public void addPoint(StrokePoint point) {
        if (points == null) {
            points = new ArrayList<>();
        }
        points.add(point);
    }
    
    public void addPoint(float x, float y, float pressure) {
        addPoint(new StrokePoint(x, y, pressure));
    }
    
    public void addPoint(float x, float y, float pressure, long timestamp) {
        addPoint(new StrokePoint(x, y, pressure, timestamp));
    }
} 