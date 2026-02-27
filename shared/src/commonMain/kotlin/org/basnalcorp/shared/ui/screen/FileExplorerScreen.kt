package org.basnalcorp.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.flowOf
import org.basnalcorp.shared.FileExplorerItem
import org.basnalcorp.shared.state.FileExplorerStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.WindowSizeClass
import org.basnalcorp.shared.ui.component.DesignListItem
import org.basnalcorp.shared.ui.component.DesignTopAppBar
import org.basnalcorp.shared.ui.component.GhostButton
import org.basnalcorp.shared.ui.component.SecondaryButton
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.theme.DesignColors
import org.basnalcorp.shared.ui.theme.DesignSpacing

@Composable
fun FileExplorerScreen(
    context: LayoutContext,
    stateHolder: FileExplorerStateHolder?,
    initialPath: String?,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(initialPath) {
        stateHolder?.load(initialPath)
    }
    LaunchedEffect(Unit) {
        stateHolder?.refresh()
    }
    val currentPath by (stateHolder?.currentPath ?: flowOf(null)).collectAsState(initial = null)
    val items by (stateHolder?.items ?: flowOf(emptyList())).collectAsState(initial = emptyList())
    val isLoading by (stateHolder?.isLoading ?: flowOf(false)).collectAsState(initial = false)
    val canGoBack = stateHolder?.canGoBack == true

    fun handleBack() {
        val holder = stateHolder
        if (canGoBack && holder != null && holder.navigateBack()) return
        onBack()
    }

    when (context.windowSizeClass) {
        WindowSizeClass.Compact,
        WindowSizeClass.Medium,
        WindowSizeClass.Expanded -> FileExplorerLayout(
            currentPath = currentPath,
            items = items,
            isLoading = isLoading,
            stateHolder = stateHolder,
            onNavigate = onNavigate,
            onBack = ::handleBack,
            canGoBack = canGoBack
        )
    }
}

@Composable
private fun FileExplorerLayout(
    currentPath: String?,
    items: List<FileExplorerItem>,
    isLoading: Boolean,
    stateHolder: FileExplorerStateHolder?,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit,
    canGoBack: Boolean
) {
    Scaffold(
        topBar = {
            DesignTopAppBar(
                title = "File explorer",
                navigationIcon = { IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.bodyLarge) } },
                actions = {
                    GhostButton(
                        text = "Refresh",
                        onClick = { stateHolder?.refresh() },
                        modifier = Modifier.padding(end = DesignSpacing.scale8)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            currentPath?.let { path ->
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DesignColors.textMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignSpacing.layoutPaddingMobile)
                )
            }
            if (isLoading && items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DesignSpacing.layoutPaddingMobile),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DesignSpacing.layoutPaddingMobile),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No files",
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignColors.textMuted
                        )
                        SecondaryButton(
                            text = "Refresh",
                            onClick = { stateHolder?.refresh() },
                            modifier = Modifier.padding(top = DesignSpacing.sectionSpacing)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = DesignSpacing.scale8),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(items, key = { it.path }) { item ->
                        DesignListItem(
                            text = if (item.isDirectory) "📁 ${item.name}" else item.name,
                            onClick = {
                                if (item.isDirectory) {
                                    stateHolder?.navigateInto(item.path)
                                } else {
                                    currentPath?.let { dir ->
                                        onNavigate(Route.InitNote(workingPath = dir))
                                    }
                                }
                            },
                            showDivider = true
                        )
                    }
                }
            }
        }
    }
}
