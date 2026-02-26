package org.basnalcorp.shared.ui

import androidx.compose.ui.unit.Dp

/**
 * Window size bucket for layout selection.
 * Thresholds aligned with Material responsive guidelines:
 * - Compact: &lt; 600dp (phone, narrow)
 * - Medium: 600dp–840dp (tablet portrait, large phone)
 * - Expanded: &gt; 840dp (tablet landscape, desktop)
 */
enum class WindowSizeClass {
    Compact,
    Medium,
    Expanded
}

/** Width breakpoint (dp) below which layout is [WindowSizeClass.Compact]. */
const val WIDTH_THRESHOLD_COMPACT_MAX_DP: Float = 600f

/** Width breakpoint (dp) below which layout is [WindowSizeClass.Medium]. Above is [WindowSizeClass.Expanded]. */
const val WIDTH_THRESHOLD_MEDIUM_MAX_DP: Float = 840f

/**
 * Maps a window width to a [WindowSizeClass].
 * Host (Android/Desktop) should call this with the current width in Dp.
 */
fun windowSizeClassFromWidth(widthDp: Dp): WindowSizeClass {
    val w = widthDp.value
    return when {
        w < WIDTH_THRESHOLD_COMPACT_MAX_DP -> WindowSizeClass.Compact
        w < WIDTH_THRESHOLD_MEDIUM_MAX_DP -> WindowSizeClass.Medium
        else -> WindowSizeClass.Expanded
    }
}
