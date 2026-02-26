package org.basnalcorp.shared.ui

/**
 * Context passed to every screen composable for layout and platform adaptation.
 * Provided by the host (Phase 5/6): Android Activity computes from WindowMetrics;
 * Desktop computes from window size.
 *
 * @param platform Android or Desktop (e.g. for back button, file picker)
 * @param windowSizeClass Compact / Medium / Expanded for layout selection
 */
data class LayoutContext(
    val platform: Platform,
    val windowSizeClass: WindowSizeClass
)
