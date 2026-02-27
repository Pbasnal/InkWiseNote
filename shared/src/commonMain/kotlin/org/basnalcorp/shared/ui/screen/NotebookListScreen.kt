package org.basnalcorp.shared.ui.screen

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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.flowOf
import org.basnalcorp.shared.appStorageRoot
import org.basnalcorp.shared.domain.SmartNotebook
import org.basnalcorp.shared.state.NotebookListStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.WindowSizeClass
import org.basnalcorp.shared.ui.component.DesignCard
import org.basnalcorp.shared.ui.component.DesignTopAppBar
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.theme.DesignColors
import org.basnalcorp.shared.ui.theme.DesignComponents
import org.basnalcorp.shared.ui.theme.DesignSpacing

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

@Composable
private fun NotebookListCompactLayout(
    notebooks: List<SmartNotebook>,
    onThemeToggle: (() -> Unit)?,
    onNavigate: (Route) -> Unit
) {
    Scaffold(
        topBar = {
            DesignTopAppBar(
                title = "Notebooks",
                actions = {
                    IconButton(onClick = { onNavigate(Route.Search) }) {
                        Box(Modifier.size(DesignComponents.topBarIconSize), contentAlignment = Alignment.Center) {
                            Text("🔍", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    IconButton(onClick = { onNavigate(Route.QueryList) }) {
                        Box(Modifier.size(DesignComponents.topBarIconSize), contentAlignment = Alignment.Center) {
                            Text("Q", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    IconButton(onClick = { onNavigate(Route.FileExplorer(initialPath = null)) }) {
                        Box(Modifier.size(DesignComponents.topBarIconSize), contentAlignment = Alignment.Center) {
                            Text("📁", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    IconButton(onClick = { onNavigate(Route.Admin) }) {
                        Box(Modifier.size(DesignComponents.topBarIconSize), contentAlignment = Alignment.Center) {
                            Text("⚙", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    IconButton(onClick = { onNavigate(Route.ChronicleTest) }) {
                        Box(Modifier.size(DesignComponents.topBarIconSize), contentAlignment = Alignment.Center) {
                            Text("C", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    if (onThemeToggle != null) {
                        IconButton(onClick = onThemeToggle) {
                            Box(Modifier.size(DesignComponents.topBarIconSize), contentAlignment = Alignment.Center) {
                                Text("🌓", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Route.InitNote(workingPath = appStorageRoot())) },
                containerColor = DesignColors.primaryBase,
                contentColor = DesignColors.textInverse,
                modifier = Modifier.sizeIn(minWidth = DesignComponents.touchTargetMin, minHeight = DesignComponents.touchTargetMin)
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
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
                    color = DesignColors.textMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(DesignSpacing.layoutPaddingMobile),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.sectionSpacing)
            ) {
                items(notebooks, key = { it.smartBook.bookId }) { notebook ->
                    NotebookCard(
                        notebook = notebook,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate(Route.SmartNotebook(bookId = notebook.smartBook.bookId)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotebookListExpandedLayout(
    notebooks: List<SmartNotebook>,
    onThemeToggle: (() -> Unit)?,
    onNavigate: (Route) -> Unit
) {
    Scaffold(
        topBar = {
            DesignTopAppBar(
                title = "Notebooks",
                actions = {
                    IconButton(onClick = { onNavigate(Route.Search) }) {
                        Box(Modifier.size(DesignComponents.topBarIconSize), contentAlignment = Alignment.Center) {
                            Text("🔍", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    IconButton(onClick = { onNavigate(Route.QueryList) }) {
                        Box(Modifier.size(DesignComponents.topBarIconSize), contentAlignment = Alignment.Center) {
                            Text("Q", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    IconButton(onClick = { onNavigate(Route.FileExplorer(initialPath = null)) }) {
                        Box(Modifier.size(DesignComponents.topBarIconSize), contentAlignment = Alignment.Center) {
                            Text("📁", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    IconButton(onClick = { onNavigate(Route.Admin) }) {
                        Box(Modifier.size(DesignComponents.topBarIconSize), contentAlignment = Alignment.Center) {
                            Text("⚙", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    IconButton(onClick = { onNavigate(Route.ChronicleTest) }) {
                        Box(Modifier.size(DesignComponents.topBarIconSize), contentAlignment = Alignment.Center) {
                            Text("C", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    if (onThemeToggle != null) {
                        IconButton(onClick = onThemeToggle) {
                            Box(Modifier.size(DesignComponents.topBarIconSize), contentAlignment = Alignment.Center) {
                                Text("🌓", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Route.InitNote(workingPath = appStorageRoot())) },
                containerColor = DesignColors.primaryBase,
                contentColor = DesignColors.textInverse,
                modifier = Modifier.sizeIn(minWidth = DesignComponents.touchTargetMin, minHeight = DesignComponents.touchTargetMin)
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
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
                    color = DesignColors.textMuted
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp), // design.json max_content_width 480; min column ~280
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(DesignSpacing.layoutPaddingMobile),
                horizontalArrangement = Arrangement.spacedBy(DesignSpacing.sectionSpacing),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.sectionSpacing)
            ) {
                items(notebooks, key = { it.smartBook.bookId }) { notebook ->
                    NotebookCard(
                        notebook = notebook,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate(Route.SmartNotebook(bookId = notebook.smartBook.bookId)) }
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

    DesignCard(modifier = modifier, onClick = onClick) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$pageCount page(s)",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
