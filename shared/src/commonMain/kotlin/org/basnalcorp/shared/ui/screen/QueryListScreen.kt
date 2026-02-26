package org.basnalcorp.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.flowOf
import org.basnalcorp.shared.domain.Query
import org.basnalcorp.shared.state.QueryListStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.WindowSizeClass
import org.basnalcorp.shared.ui.component.DesignCard
import org.basnalcorp.shared.ui.component.DesignTopAppBar
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.theme.DesignSpacing

@Composable
fun QueryListScreen(
    context: LayoutContext,
    stateHolder: QueryListStateHolder?,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    var queries by remember { mutableStateOf<List<Query>>(emptyList()) }
    LaunchedEffect(stateHolder) {
        (stateHolder?.queries ?: flowOf(emptyList())).collect { queries = it }
    }
    when (context.windowSizeClass) {
        WindowSizeClass.Compact -> QueryListCompactLayout(queries = queries, onBack = onBack, onNavigate = onNavigate)
        WindowSizeClass.Medium, WindowSizeClass.Expanded -> QueryListExpandedLayout(queries = queries, onBack = onBack, onNavigate = onNavigate)
    }
}

@Composable
private fun QueryListCompactLayout(
    queries: List<Query>,
    onBack: () -> Unit,
    onNavigate: (Route) -> Unit
) {
    Scaffold(
        topBar = {
            DesignTopAppBar(
                title = "Saved queries",
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        if (queries.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No saved queries", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(DesignSpacing.layoutPaddingMobile),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.sectionSpacing)
            ) {
                items(queries, key = { it.name }) { query ->
                    DesignCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate(Route.QueryResults(query.name)) }
                    ) {
                        Text(query.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun QueryListExpandedLayout(
    queries: List<Query>,
    onBack: () -> Unit,
    onNavigate: (Route) -> Unit
) {
    Scaffold(
        topBar = {
            DesignTopAppBar(
                title = "Saved queries",
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        if (queries.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No saved queries", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(DesignSpacing.layoutPaddingMobile),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.sectionSpacing)
            ) {
                items(queries, key = { it.name }) { query ->
                    DesignCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate(Route.QueryResults(query.name)) }
                    ) {
                        Text(query.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
