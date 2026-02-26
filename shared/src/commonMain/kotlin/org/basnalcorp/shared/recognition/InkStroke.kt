package org.basnalcorp.shared.recognition

/**
 * Single stroke for digital ink recognition (Phase 9.1).
 * Platform (Android ML Kit) converts list of strokes to text.
 */
data class InkStroke(
    val points: List<InkPoint>
)

data class InkPoint(
    val x: Float,
    val y: Float,
    val timestampMs: Long = 0L
)
