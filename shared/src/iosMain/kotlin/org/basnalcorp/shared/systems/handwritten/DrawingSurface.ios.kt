package org.basnalcorp.shared.systems.handwritten

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun DrawingSurface(
    modifier: Modifier,
    strokes: List<Stroke>,
    onStrokesChanged: (List<Stroke>) -> Unit,
    tool: DrawingTool,
    penConfig: PenConfig,
    config: HandwrittenConfig
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Drawing not yet supported on iOS")
    }
}
