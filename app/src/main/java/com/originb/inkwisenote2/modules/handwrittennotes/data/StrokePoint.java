package com.originb.inkwisenote2.modules.handwrittennotes.data;

import java.io.Serializable;

/**
 * Represents a single point in a stroke with coordinates, pressure and timestamp
 */
public class StrokePoint implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private float x;
    private float y;
    private float pressure;
    private long timestamp;
    
    public StrokePoint() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public StrokePoint(float x, float y, float pressure) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.timestamp = System.currentTimeMillis();
    }
    
    public StrokePoint(float x, float y, float pressure, long timestamp) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.timestamp = timestamp;
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public float getPressure() {
        return pressure;
    }
    
    public void setPressure(float pressure) {
        this.pressure = pressure;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 