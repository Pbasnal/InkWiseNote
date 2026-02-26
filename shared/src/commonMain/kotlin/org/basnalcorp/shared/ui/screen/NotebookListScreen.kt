package org.basnalcorp.shared.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.flowOf
import org.basnalcorp.shared.domain.SmartNotebook
import org.basnalcorp.shared.state.NotebookListStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.WindowSizeClass
import org.basnalcorp.shared.ui.nav.Route

/**
 * Pilot screen (Phase 6.1): notebook list with Compact and Expanded layouts.
 * Selects layout via [context.windowSizeClass].
 */
@Composable
fun NotebookListScreen(
    context: LayoutContext,
    stateHolder: NotebookListStateHolder?,
    onNavigate: (Route) -> Unit,
    onThemeToggle: (() -> Unit)?
) {
    var notebookList by remember { mutableStateOf<List<SmartNotebook>>(emptyList()) }
    LaunchedEffect(stateHolder) {
        (stateHolder?.notebooks ?: flowOf(emptyList())).collect { notebookList = it }
    }

    when (context.windowSizeClass) {
        WindowSizeClass.Compact -> NotebookListCompactLayout(
            notebooks = notebookList,
            onThemeToggle = onThemeToggle,
            onNavigate = onNavigate
        )
        WindowSizeClass.Medium,
        WindowSizeClass.Expanded -> NotebookListExpandedLayout(
            notebooks = notebookList,
            onThemeToggle = onThemeToggle,
            onNavigate = onNavigate
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotebookListCompactLayout(
    notebooks: List<SmartNotebook>,
    onThemeToggle: (() -> Unit)?,
    onNavigate: (Route) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notebooks") },
                actions = {
                    if (onThemeToggle != null) {
                        IconButton(onClick = onThemeToggle) {
                            Text("🌓", style = MaterialTheme.typography.bodyLarge) // Theme toggle (Phase 6.4)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notebooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No notebooks yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notebooks, key = { it.smartBook.bookId }) { notebook ->
                    NotebookCard(
                        notebook = notebook,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { /* Phase 7: navigate to notebook detail */ }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotebookListExpandedLayout(
    notebooks: List<SmartNotebook>,
    onThemeToggle: (() -> Unit)?,
    onNavigate: (Route) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notebooks") },
                actions = {
                    if (onThemeToggle != null) {
                        IconButton(onClick = onThemeToggle) {
                            Text("🌓", style = MaterialTheme.typography.bodyLarge) // Theme toggle (Phase 6.4)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notebooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No notebooks yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(notebooks, key = { it.smartBook.bookId }) { notebook ->
                    NotebookCard(
                        notebook = notebook,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { /* Phase 7: navigate to notebook detail */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotebookCard(
    notebook: SmartNotebook,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val title = notebook.smartBook.title?.takeIf { it.isNotBlank() } ?: "Untitled"
    val pageCount = notebook.smartBookPages.size

    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$pageCount page(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
