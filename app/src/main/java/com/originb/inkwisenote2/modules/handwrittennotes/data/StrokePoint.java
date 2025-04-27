package com.originb.inkwisenote2.modules.handwrittennotes.data;

import java.io.Serializable;

/**
 * Represents a single point in a stroke with coordinates and pressure
 */
public class StrokePoint implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private float x;
    private float y;
    private float pressure;
    
    public StrokePoint() {
    }
    
    public StrokePoint(float x, float y, float pressure) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
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
} 