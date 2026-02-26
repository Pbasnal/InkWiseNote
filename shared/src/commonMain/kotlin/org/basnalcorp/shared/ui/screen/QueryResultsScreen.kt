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

@Composable
fun QueryResultsScreen(
    context: LayoutContext,
    queryName: String,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    when (context.windowSizeClass) {
        WindowSizeClass.Compact -> QueryResultsCompactLayout(queryName = queryName, onBack = onBack)
        WindowSizeClass.Medium, WindowSizeClass.Expanded -> QueryResultsExpandedLayout(queryName = queryName, onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueryResultsCompactLayout(queryName: String, onBack: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(
            title = { Text("Results: $queryName") },
            navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
        ) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Query results – TBD", Modifier.padding(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueryResultsExpandedLayout(queryName: String, onBack: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(
            title = { Text("Results: $queryName") },
            navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
        ) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Query results – TBD", Modifier.padding(16.dp))
        }
    }
}
