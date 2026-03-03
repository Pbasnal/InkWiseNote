package org.basnalcorp.shared.systems.handwritten

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Tool for the drawing surface: Pen (draw) or Eraser (whole-stroke removal).
 */
enum class DrawingTool { Pen, Eraser }

/**
 * Pen appearance: color (ARGB as Long) and stroke width in logical units.
 */
/** Converts ARGB long to Compose Color. */
fun Long.toComposeColor(): Color = Color(
    alpha = ((this shr 24) and 0xFF) / 255f,
    red = ((this shr 16) and 0xFF) / 255f,
    green = ((this shr 8) and 0xFF) / 255f,
    blue = (this and 0xFF) / 255f
)

data class PenConfig(
    val color: Long,
    val width: Float
) {
    fun toComposeColor(): Color = color.toComposeColor()
}
