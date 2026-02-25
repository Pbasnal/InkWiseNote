package com.originb.inkwisenote2.utils

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.MotionEvent.PointerProperties
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.originb.inkwisenote2.modules.handwrittennotes.ui.DrawingView
import com.originb.inkwisenote2.testutils.HandwritingStrokeHelper
import org.hamcrest.Matcher
import kotlin.math.cos
import kotlin.math.sin

object DrawingTestUtils {
    fun drawStroke(description: String, points: MutableList<HandwritingStrokeHelper.Point?>): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View?> {
                return ViewMatchers.isAssignableFrom(DrawingView::class.java)
            }

            override fun getDescription(): String {
                return description
            }

            override fun perform(uiController: UiController, view: View) {
                val drawingView = view as DrawingView?
                var downEvent: MotionEvent? = null
                var moveEvent: MotionEvent? = null

                // Obtain the absolute coordinates of the view
                val locationOnScreen = IntArray(2)
                view.getLocationOnScreen(locationOnScreen)

                try {
                    // Setup pointer properties for stylus
                    val properties = arrayOfNulls<PointerProperties>(1)
                    properties[0] = PointerProperties()
                    properties[0]!!.id = 0
                    properties[0]!!.toolType = MotionEvent.TOOL_TYPE_STYLUS

                    // Setup pointer coordinates
                    val coords = arrayOfNulls<MotionEvent.PointerCoords>(1)
                    coords[0] = MotionEvent.PointerCoords()

                    // Initial touch
                    coords[0]!!.x = points.get(0)!!.x + locationOnScreen[0]
                    coords[0]!!.y = points.get(0)!!.y + locationOnScreen[1]
                    coords[0]!!.pressure = 1.0f
                    coords[0]!!.size = 1.0f

                    val downTime = SystemClock.uptimeMillis()

                    // Create down event
                    downEvent = MotionEvent.obtain(
                        downTime,
                        downTime,
                        MotionEvent.ACTION_DOWN,
                        1,  // pointer count
                        properties,
                        coords,
                        0,  // metaState
                        0,  // buttonState
                        1.0f,  // xPrecision
                        1.0f,  // yPrecision
                        0,  // deviceId
                        0,  // edgeFlags
                        InputDevice.SOURCE_STYLUS,
                        0 // flags
                    )

                    drawingView!!.dispatchTouchEvent(downEvent)
                    uiController.loopMainThreadForAtLeast(50)

                    // Move touch
                    for (i in 1..<points.size) {
                        coords[0]!!.x = points.get(i)!!.x + locationOnScreen[0]
                        coords[0]!!.y = points.get(i)!!.y + locationOnScreen[1]

                        moveEvent = MotionEvent.obtain(
                            downTime,
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_MOVE,
                            1,  // pointer count
                            properties,
                            coords,
                            0,  // metaState
                            0,  // buttonState
                            1.0f,  // xPrecision
                            1.0f,  // yPrecision
                            0,  // deviceId
                            0,  // edgeFlags
                            InputDevice.SOURCE_STYLUS,
                            0 // flags
                        )

                        drawingView.dispatchTouchEvent(moveEvent)
                        uiController.loopMainThreadForAtLeast(1) //16 ~60fps
                    }

                    // Release touch
                    val upEvent = MotionEvent.obtain(
                        downTime,
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP,
                        1,  // pointer count
                        properties,
                        coords,
                        0,  // metaState
                        0,  // buttonState
                        1.0f,  // xPrecision
                        1.0f,  // yPrecision
                        0,  // deviceId
                        0,  // edgeFlags
                        InputDevice.SOURCE_STYLUS,
                        0 // flags
                    )

                    drawingView.dispatchTouchEvent(upEvent)
                    uiController.loopMainThreadForAtLeast(10)
                    upEvent.recycle()
                } finally {
                    if (downEvent != null) downEvent.recycle()
                    if (moveEvent != null) moveEvent.recycle()
                }
            }
        }
    }

    fun createStraightLine(): MutableList<HandwritingStrokeHelper.Point?> {
        val points: MutableList<HandwritingStrokeHelper.Point?> = ArrayList<HandwritingStrokeHelper.Point?>()
        for (i in 0..99) {
            points.add(HandwritingStrokeHelper.Point((100 + i).toFloat(), 100f))
        }
        return points
    }

    fun createZigzagLine(): MutableList<HandwritingStrokeHelper.Point?> {
        val points: MutableList<HandwritingStrokeHelper.Point?> = ArrayList<HandwritingStrokeHelper.Point?>()
        for (i in 0..99) {
            points.add(HandwritingStrokeHelper.Point((300 + i).toFloat(), (300 + (i % 2) * 50).toFloat()))
        }
        return points
    }

    fun createCircle(): MutableList<HandwritingStrokeHelper.Point?> {
        val points: MutableList<HandwritingStrokeHelper.Point?> = ArrayList<HandwritingStrokeHelper.Point?>()
        val centerX = 500f
        val centerY = 500f
        val radius = 100f

        var i = 0
        while (i <= 360) {
            val angle = Math.toRadians(i.toDouble())
            val x = centerX + (radius * cos(angle)).toFloat()
            val y = centerY + (radius * sin(angle)).toFloat()
            points.add(HandwritingStrokeHelper.Point(x, y))
            i += 5
        }
        return points
    }
}
