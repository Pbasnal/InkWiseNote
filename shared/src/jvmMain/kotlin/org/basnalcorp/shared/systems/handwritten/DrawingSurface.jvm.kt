package org.basnalcorp.shared.systems.handwritten

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.Stroke as StrokeStyle
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.min

@Composable
actual fun DrawingSurface(
    modifier: Modifier,
    strokes: List<Stroke>,
    onStrokesChanged: (List<Stroke>) -> Unit,
    tool: DrawingTool,
    penConfig: PenConfig,
    config: HandwrittenConfig
) {
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    val scaleAndOffset = letterbox(
        viewWidth = viewSize.width.toFloat(),
        viewHeight = viewSize.height.toFloat(),
        logicalWidth = config.canvasLogicalWidth,
        logicalHeight = config.canvasLogicalHeight
    )
    val currentStrokePoints = remember { mutableStateListOf<StrokePoint>() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { viewSize = it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(tool, scaleAndOffset, strokes, onStrokesChanged) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val logical = viewToLogical(offset, scaleAndOffset)
                            when (tool) {
                                DrawingTool.Pen -> {
                                    currentStrokePoints.clear()
                                    currentStrokePoints.add(
                                        StrokePoint(
                                            x = logical.x,
                                            y = logical.y,
                                            pressure = 1f,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                }
                                DrawingTool.Eraser -> {
                                    val toRemove = hitTestStroke(logical, strokes)
                                    if (toRemove >= 0) {
                                        onStrokesChanged(strokes.filterIndexed { i, _ -> i != toRemove })
                                    }
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            val logical = viewToLogical(change.position, scaleAndOffset)
                            when (tool) {
                                DrawingTool.Pen -> {
                                    currentStrokePoints.add(
                                        StrokePoint(
                                            x = logical.x,
                                            y = logical.y,
                                            pressure = 1f,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                }
                                DrawingTool.Eraser -> {}
                            }
                        },
                        onDragEnd = {
                            when (tool) {
                                DrawingTool.Pen -> {
                                    if (currentStrokePoints.isNotEmpty()) {
                                        val newStroke = Stroke(
                                            color = penConfig.color,
                                            width = penConfig.width,
                                            points = currentStrokePoints.toList()
                                        )
                                        onStrokesChanged(strokes.plus(newStroke))
                                        currentStrokePoints.clear()
                                    }
                                }
                                DrawingTool.Eraser -> {}
                            }
                        }
                    )
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val scale = scaleAndOffset.scale
                val offsetX = scaleAndOffset.offsetInViewX
                val offsetY = scaleAndOffset.offsetInViewY
                translate(offsetX, offsetY) {
                    scale(scale) {
                        strokes.forEach { stroke ->
                            drawStroke(stroke)
                        }
                        if (currentStrokePoints.isNotEmpty()) {
                            drawStroke(
                                Stroke(
                                    color = penConfig.color,
                                    width = penConfig.width,
                                    points = currentStrokePoints.toList()
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class ScaleAndOffset(
    val scale: Float,
    val offsetInViewX: Float,
    val offsetInViewY: Float
)

private fun letterbox(
    viewWidth: Float,
    viewHeight: Float,
    logicalWidth: Float,
    logicalHeight: Float
): ScaleAndOffset {
    if (viewWidth <= 0 || viewHeight <= 0) return ScaleAndOffset(1f, 0f, 0f)
    val scale = min(viewWidth / logicalWidth, viewHeight / logicalHeight)
    val offsetInViewX = (viewWidth - logicalWidth * scale) / 2f
    val offsetInViewY = (viewHeight - logicalHeight * scale) / 2f
    return ScaleAndOffset(scale, offsetInViewX, offsetInViewY)
}

private fun viewToLogical(view: Offset, so: ScaleAndOffset): Offset =
    Offset(
        (view.x - so.offsetInViewX) / so.scale,
        (view.y - so.offsetInViewY) / so.scale
    )

private fun hitTestStroke(point: Offset, strokes: List<Stroke>, threshold: Float = 20f): Int {
    strokes.forEachIndexed { index, stroke ->
        stroke.points.forEach { p ->
            val dx = p.x - point.x
            val dy = p.y - point.y
            if (dx * dx + dy * dy <= threshold * threshold) return index
        }
    }
    return -1
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStroke(stroke: Stroke) {
    if (stroke.points.size < 2) return
    val path = Path().apply {
        moveTo(stroke.points[0].x, stroke.points[0].y)
        stroke.points.drop(1).forEach { point -> lineTo(point.x, point.y) }
    }
    drawPath(
        path = path,
        color = stroke.color.toComposeColor(),
        style = StrokeStyle(
            width = stroke.width,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
