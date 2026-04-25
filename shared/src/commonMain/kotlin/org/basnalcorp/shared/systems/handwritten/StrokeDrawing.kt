package org.basnalcorp.shared.systems.handwritten

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.Stroke as StrokeStyle

/**
 * Draws a single stroke in logical coordinates (call inside a scope that has already applied letterbox transform).
 */
fun DrawScope.drawStroke(stroke: Stroke) {
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

/**
 * Applies letterbox transform then draws all [strokes] and, if non-empty, the in-progress stroke from [currentStrokePoints] with [penConfig].
 */
fun DrawScope.drawStrokes(
    letterbox: Letterbox,
    strokes: List<Stroke>,
    currentStrokePoints: List<StrokePoint>,
    penConfig: PenConfig
) {
    translate(letterbox.offsetInViewX, letterbox.offsetInViewY) {
        scale(letterbox.scale) {
            strokes.forEach { stroke -> drawStroke(stroke) }
            if (currentStrokePoints.isNotEmpty()) {
                drawStroke(
                    Stroke(
                        color = penConfig.color,
                        width = penConfig.width,
                        points = currentStrokePoints
                    )
                )
            }
        }
    }
}
