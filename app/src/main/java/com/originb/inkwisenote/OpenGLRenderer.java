package com.originb.inkwisenote;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;
import java.util.List;

public class OpenGLRenderer implements GLSurfaceView.Renderer {
    private final List<float[]> points = new ArrayList<>();
    private float[] projectionMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        // Initialize shaders and buffers
        initShaders();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // Draw the points
        drawPoints();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Matrix.orthoM(projectionMatrix, 0, 0, width, height, 0, -1, 1);
    }

    public void addPoint(float x, float y) {
        points.add(new float[]{x, y});
    }

    private void initShaders() {
        // Initialize your vertex and fragment shaders here
    }

    private void drawPoints() {
        // Use your shaders and buffers to draw the points
    }
}