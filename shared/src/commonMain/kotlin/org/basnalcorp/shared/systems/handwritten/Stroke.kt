package org.basnalcorp.shared.systems.handwritten

/**
 * A single point in a stroke: logical coordinates, pressure, and timestamp.
 * All coordinates and dimensions are in the canvas logical coordinate system.
 */
data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float,
    val timestamp: Long
)

/**
 * A single stroke: color (ARGB), width in logical units, and ordered points.
 */
data class Stroke(
    val color: Long,
    val width: Float,
    val points: List<StrokePoint>
) {
    init {
        require(points.isNotEmpty()) { "Stroke must have at least one point" }
    }
}
