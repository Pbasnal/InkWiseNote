package com.originb.inkwisenote;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class OpenGLView extends GLSurfaceView {
    private final OpenGLRenderer renderer;

    public OpenGLView(Context context) {
        super(context);
        // Set the OpenGL ES version
        setEGLContextClientVersion(2);

        // Initialize the Renderer
        renderer = new OpenGLRenderer();
        setRenderer(renderer);

        // Render only when there is a change in the drawing
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    // Public method to add points to the path
    public void addPoint(float x, float y) {
        renderer.addPoint(x, y);
        requestRender(); // Request rendering
    }
}
