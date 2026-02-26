package org.basnalcorp.shared.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.WindowSizeClass
import org.basnalcorp.shared.ui.nav.Route

// --- QueryCreationScreen ---

@Composable
fun QueryCreationScreen(
    context: LayoutContext,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    when (context.windowSizeClass) {
        WindowSizeClass.Compact -> StubLayout("New query", "TBD", onBack)
        WindowSizeClass.Medium, WindowSizeClass.Expanded -> StubLayout("New query", "TBD", onBack)
    }
}

// --- AdminScreen ---

@Composable
fun AdminScreen(
    context: LayoutContext,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    when (context.windowSizeClass) {
        WindowSizeClass.Compact -> StubLayout("Admin", "TBD", onBack)
        WindowSizeClass.Medium, WindowSizeClass.Expanded -> StubLayout("Admin", "TBD", onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StubLayout(title: String, subtitle: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("$title – $subtitle (TBD)", Modifier.padding(16.dp))
        }
    }
}
