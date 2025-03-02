package com.originb.inkwisenote.utils;

import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import com.originb.inkwisenote.testutils.HandwritingStrokeHelper;
import com.originb.inkwisenote.ux.views.DrawingView;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;

public class DrawingTestUtils {

    public static ViewAction drawStroke(String description, List<HandwritingStrokeHelper.Point> points) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(DrawingView.class);
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public void perform(UiController uiController, View view) {
                DrawingView drawingView = (DrawingView) view;
                MotionEvent downEvent = null;
                MotionEvent moveEvent = null;

                // Obtain the absolute coordinates of the view
                int[] locationOnScreen = new int[2];
                view.getLocationOnScreen(locationOnScreen);

                try {
                    // Setup pointer properties for stylus
                    MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[1];
                    properties[0] = new MotionEvent.PointerProperties();
                    properties[0].id = 0;
                    properties[0].toolType = MotionEvent.TOOL_TYPE_STYLUS;

                    // Setup pointer coordinates
                    MotionEvent.PointerCoords[] coords = new MotionEvent.PointerCoords[1];
                    coords[0] = new MotionEvent.PointerCoords();

                    // Initial touch
                    coords[0].x = points.get(0).x + locationOnScreen[0];
                    coords[0].y = points.get(0).y + locationOnScreen[1];
                    coords[0].pressure = 1.0f;
                    coords[0].size = 1.0f;

                    long downTime = SystemClock.uptimeMillis();

                    // Create down event
                    downEvent = MotionEvent.obtain(
                            downTime,
                            downTime,
                            MotionEvent.ACTION_DOWN,
                            1, // pointer count
                            properties,
                            coords,
                            0, // metaState
                            0, // buttonState
                            1.0f, // xPrecision
                            1.0f, // yPrecision
                            0, // deviceId
                            0, // edgeFlags
                            InputDevice.SOURCE_STYLUS,
                            0 // flags
                    );

                    drawingView.dispatchTouchEvent(downEvent);
                    uiController.loopMainThreadForAtLeast(50);

                    // Move touch
                    for (int i = 1; i < points.size(); i++) {
                        coords[0].x = points.get(i).x + locationOnScreen[0];
                        coords[0].y = points.get(i).y + locationOnScreen[1];

                        moveEvent = MotionEvent.obtain(
                                downTime,
                                SystemClock.uptimeMillis(),
                                MotionEvent.ACTION_MOVE,
                                1, // pointer count
                                properties,
                                coords,
                                0, // metaState
                                0, // buttonState
                                1.0f, // xPrecision
                                1.0f, // yPrecision
                                0, // deviceId
                                0, // edgeFlags
                                InputDevice.SOURCE_STYLUS,
                                0 // flags
                        );

                        drawingView.dispatchTouchEvent(moveEvent);
                        uiController.loopMainThreadForAtLeast(1); //16 ~60fps
                    }

                    // Release touch
                    MotionEvent upEvent = MotionEvent.obtain(
                            downTime,
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_UP,
                            1, // pointer count
                            properties,
                            coords,
                            0, // metaState
                            0, // buttonState
                            1.0f, // xPrecision
                            1.0f, // yPrecision
                            0, // deviceId
                            0, // edgeFlags
                            InputDevice.SOURCE_STYLUS,
                            0 // flags
                    );

                    drawingView.dispatchTouchEvent(upEvent);
                    uiController.loopMainThreadForAtLeast(10);
                    upEvent.recycle();

                } finally {
                    if (downEvent != null) downEvent.recycle();
                    if (moveEvent != null) moveEvent.recycle();
                }
            }
        };
    }

    public static List<HandwritingStrokeHelper.Point> createStraightLine() {
        List<HandwritingStrokeHelper.Point> points = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            points.add(new HandwritingStrokeHelper.Point(100 + i, 100));
        }
        return points;
    }

    public static List<HandwritingStrokeHelper.Point> createZigzagLine() {
        List<HandwritingStrokeHelper.Point> points = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            points.add(new HandwritingStrokeHelper.Point(300 + i, 300 + (i % 2) * 50));
        }
        return points;
    }

    public static List<HandwritingStrokeHelper.Point> createCircle() {
        List<HandwritingStrokeHelper.Point> points = new ArrayList<>();
        float centerX = 500;
        float centerY = 500;
        float radius = 100;

        for (int i = 0; i <= 360; i += 5) {
            double angle = Math.toRadians(i);
            float x = centerX + (float) (radius * Math.cos(angle));
            float y = centerY + (float) (radius * Math.sin(angle));
            points.add(new HandwritingStrokeHelper.Point(x, y));
        }
        return points;
    }
}
