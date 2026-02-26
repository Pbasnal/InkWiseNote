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
import kotlinx.coroutines.flow.flowOf
import org.basnalcorp.shared.domain.SmartNotebook
import org.basnalcorp.shared.state.RelatedNotesStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.WindowSizeClass
import org.basnalcorp.shared.ui.component.DesignCard
import org.basnalcorp.shared.ui.component.DesignTopAppBar
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.theme.DesignColors
import org.basnalcorp.shared.ui.theme.DesignSpacing

@Composable
fun RelatedNotesScreen(
    context: LayoutContext,
    stateHolder: RelatedNotesStateHolder?,
    bookId: Long,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(bookId) {
        stateHolder?.load(bookId)
    }
    val relatedNotebooks by (stateHolder?.relatedNotebooks ?: flowOf(emptyList())).collectAsState(initial = emptyList())
    val isLoading by (stateHolder?.isLoading ?: flowOf(false)).collectAsState(initial = false)

    when (context.windowSizeClass) {
        WindowSizeClass.Compact,
        WindowSizeClass.Medium,
        WindowSizeClass.Expanded -> RelatedNotesLayout(
            relatedNotebooks = relatedNotebooks,
            isLoading = isLoading,
            onNavigate = onNavigate,
            onBack = onBack
        )
    }
}

@Composable
private fun RelatedNotesLayout(
    relatedNotebooks: List<SmartNotebook>,
    isLoading: Boolean,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            DesignTopAppBar(
                title = "Related notes",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && relatedNotebooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (relatedNotebooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No related notes",
                    style = MaterialTheme.typography.bodySmall,
                    color = DesignColors.textMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(DesignSpacing.layoutPaddingMobile),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.sectionSpacing)
            ) {
                items(relatedNotebooks, key = { it.smartBook.bookId }) { notebook ->
                    RelatedNoteCard(
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
private fun RelatedNoteCard(
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
