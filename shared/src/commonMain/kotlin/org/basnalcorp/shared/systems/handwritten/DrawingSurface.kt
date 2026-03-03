package org.basnalcorp.shared.systems.handwritten

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shared contract for the drawing surface. Renders a fixed-aspect canvas (e.g. A4), letterboxed
 * into [modifier]. Touch/pointer → logical coords; Pen appends points and reports via
 * [onStrokesChanged]; Eraser removes whole strokes. No knowledge of noteId, paths, or save timing.
 */
@Composable
expect fun DrawingSurface(
    modifier: Modifier = Modifier,
    strokes: List<Stroke>,
    onStrokesChanged: (List<Stroke>) -> Unit,
    tool: DrawingTool,
    penConfig: PenConfig,
    config: HandwrittenConfig = HandwrittenConfig.default()
)
