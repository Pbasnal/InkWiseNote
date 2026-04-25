package org.basnalcorp.shared.systems.handwritten

import androidx.compose.ui.geometry.Offset
import kotlin.math.min

/**
 * Letterbox transform: scale and offsets in view pixels for drawing the logical canvas
 * centered and aspect-fit inside the view.
 */
data class Letterbox(
    val scale: Float,
    val offsetInViewX: Float,
    val offsetInViewY: Float
)

/**
 * Computes letterbox so the logical canvas fits inside the view (aspect-fit) and is centered.
 * @return Letterbox with scale and offsets in view pixels; safe default if view size <= 0.
 */
fun letterbox(
    viewWidthPx: Float,
    viewHeightPx: Float,
    logicalWidth: Float,
    logicalHeight: Float
): Letterbox {
    if (viewWidthPx <= 0f || viewHeightPx <= 0f) return Letterbox(1f, 0f, 0f)
    val scale = min(viewWidthPx / logicalWidth, viewHeightPx / logicalHeight)
    val offsetInViewX = (viewWidthPx - logicalWidth * scale) / 2f
    val offsetInViewY = (viewHeightPx - logicalHeight * scale) / 2f
    return Letterbox(scale, offsetInViewX, offsetInViewY)
}

/**
 * Converts a point from view-local pixels to logical coordinates using the letterbox transform.
 */
fun viewToLogical(viewPx: Offset, letterbox: Letterbox): Offset =
    Offset(
        (viewPx.x - letterbox.offsetInViewX) / letterbox.scale,
        (viewPx.y - letterbox.offsetInViewY) / letterbox.scale
    )
