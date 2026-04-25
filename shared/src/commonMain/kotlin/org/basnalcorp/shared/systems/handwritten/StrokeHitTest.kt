package org.basnalcorp.shared.systems.handwritten

import androidx.compose.ui.geometry.Offset

/**
 * Finds the first stroke that has any point within [threshold] (logical units) of [pointLogical].
 * @return Index of the hit stroke, or -1 if none.
 */
fun hitTestStroke(
    pointLogical: Offset,
    strokes: List<Stroke>,
    threshold: Float = 20f
): Int {
    val thresholdSq = threshold * threshold
    strokes.forEachIndexed { index, stroke ->
        for (p in stroke.points) {
            val dx = p.x - pointLogical.x
            val dy = p.y - pointLogical.y
            if (dx * dx + dy * dy <= thresholdSq) return index
        }
    }
    return -1
}
