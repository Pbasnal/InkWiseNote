package org.basnalcorp.shared.systems.handwritten

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.time.Clock

/**
 * Holds in-progress stroke state and handles drag events from the platform.
 * Platform calls handleDragStart / handleDrag / handleDragEnd with view-local px and pressure;
 * this handler converts to logical coords, updates [currentStrokePoints], and invokes [onStrokesChanged].
 */
class DrawingTouchHandler {

    /** In-progress stroke points (logical). Mutations trigger Compose recomposition when read in UI. */
    val currentStrokePoints: SnapshotStateList<StrokePoint> = mutableStateListOf()

    fun handleDragStart(
        viewLocalPx: Offset,
        pressure: Float,
        letterbox: Letterbox,
        tool: DrawingTool,
        penConfig: PenConfig,
        strokes: List<Stroke>,
        onStrokesChanged: (List<Stroke>) -> Unit
    ) {
        val logical = viewToLogical(viewLocalPx, letterbox)
        when (tool) {
            DrawingTool.Pen -> {
                currentStrokePoints.clear()
                currentStrokePoints.add(
                    StrokePoint(
                        x = logical.x,
                        y = logical.y,
                        pressure = pressure,
                        timestamp = Clock.System.now().toEpochMilliseconds()
                    )
                )
            }
            DrawingTool.Eraser -> {
                val idx = hitTestStroke(logical, strokes)
                if (idx >= 0) {
                    onStrokesChanged(strokes.filterIndexed { i, _ -> i != idx })
                }
            }
        }
    }

    fun handleDrag(
        viewLocalPx: Offset,
        pressure: Float,
        letterbox: Letterbox,
        tool: DrawingTool
    ) {
        when (tool) {
            DrawingTool.Pen -> {
                val logical = viewToLogical(viewLocalPx, letterbox)
                currentStrokePoints.add(
                    StrokePoint(
                        x = logical.x,
                        y = logical.y,
                        pressure = pressure,
                        timestamp = Clock.System.now().toEpochMilliseconds()
                    )
                )
            }
            DrawingTool.Eraser -> { /* no-op */ }
        }
    }

    fun handleDragEnd(
        tool: DrawingTool,
        penConfig: PenConfig,
        strokes: List<Stroke>,
        onStrokesChanged: (List<Stroke>) -> Unit
    ) {
        when (tool) {
            DrawingTool.Pen -> {
                if (currentStrokePoints.isNotEmpty()) {
                    val newStroke = Stroke(
                        color = penConfig.color,
                        width = penConfig.width,
                        points = currentStrokePoints.toList()
                    )
                    onStrokesChanged(strokes + newStroke)
                    currentStrokePoints.clear()
                }
            }
            DrawingTool.Eraser -> { /* no-op */ }
        }
    }
}
