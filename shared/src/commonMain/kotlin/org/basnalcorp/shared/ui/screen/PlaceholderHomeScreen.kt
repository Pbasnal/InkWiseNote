package org.basnalcorp.shared.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.nav.Route

/**
 * Placeholder home screen for Phase 4.
 * Replaced by real NotebookListScreen in Phase 6.
 */
@Composable
fun PlaceholderHomeScreen(
    context: LayoutContext,
    onNavigate: (Route) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Home (placeholder)")
    }
}
