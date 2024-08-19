package com.example.hellodroid;

import android.os.Bundle;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HelloDroidActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the layout defined in activity_main.xml
        setContentView(R.layout.activity_main);

        // Find the custom DrawingView by its ID
        DrawingView drawingView = findViewById(R.id.drawing_view);

        // Any additional initialization can be done here
    }

//    private OpenGLView openGLView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        openGLView = new OpenGLView(this);
//        setContentView(openGLView);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        float x = event.getX();
//        float y = event.getY();
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//            case MotionEvent.ACTION_MOVE:
//                openGLView.addPoint(x, y);
//                return true;
//        }
//        return super.onTouchEvent(event);
//    }
}
