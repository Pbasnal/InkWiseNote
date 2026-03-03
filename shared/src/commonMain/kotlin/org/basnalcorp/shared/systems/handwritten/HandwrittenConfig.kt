package org.basnalcorp.shared.systems.handwritten

/**
 * Canvas and pen defaults for the handwritten drawing system.
 * Canvas size is in logical units; strokes are stored in this coordinate system.
 */
data class HandwrittenConfig(
    val defaultColor: Long,
    val defaultThickness: Float,
    val canvasLogicalWidth: Float,
    val canvasLogicalHeight: Float,
    val dpi: Int
) {
    companion object {
        /** A4 at 72 DPI (logical units). */
        const val CANVAS_DPI = 72
        const val DEFAULT_CANVAS_LOGICAL_WIDTH = 595f
        const val DEFAULT_CANVAS_LOGICAL_HEIGHT = 842f

        /** Default pen: blue (ARGB), thickness 3. */
        const val DEFAULT_PEN_COLOR = 0xFF0000FFL
        const val DEFAULT_PEN_THICKNESS = 3f

        /** Default config (no config file in v1). */
        fun default(): HandwrittenConfig = HandwrittenConfig(
            defaultColor = DEFAULT_PEN_COLOR,
            defaultThickness = DEFAULT_PEN_THICKNESS,
            canvasLogicalWidth = DEFAULT_CANVAS_LOGICAL_WIDTH,
            canvasLogicalHeight = DEFAULT_CANVAS_LOGICAL_HEIGHT,
            dpi = CANVAS_DPI
        )
    }
}
