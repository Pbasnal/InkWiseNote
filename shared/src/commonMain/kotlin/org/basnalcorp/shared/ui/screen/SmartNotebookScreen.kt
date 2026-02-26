package org.basnalcorp.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.flowOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.basnalcorp.shared.domain.AtomicNote
import org.basnalcorp.shared.domain.SmartNotebook
import org.basnalcorp.shared.state.SmartNotebookStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.WindowSizeClass
import org.basnalcorp.shared.ui.component.DesignCard
import org.basnalcorp.shared.ui.component.DesignTopAppBar
import org.basnalcorp.shared.ui.component.GhostButton
import org.basnalcorp.shared.ui.component.PrimaryButton
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.theme.DesignSpacing

// One note per page; Prev/Next change page (no horizontal swipe to avoid accidental movement).

@Composable
fun SmartNotebookScreen(
    context: LayoutContext,
    stateHolder: SmartNotebookStateHolder?,
    bookId: Long?,
    workingPath: String?,
    bookTitle: String?,
    selectedNoteId: Long?,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(bookId, workingPath, bookTitle, selectedNoteId) {
        stateHolder?.load(bookId, workingPath, bookTitle, selectedNoteId)
    }
    val notebookFlow = stateHolder?.notebook ?: flowOf(null)
    val notebook by notebookFlow.collectAsState(initial = null)

    when (context.windowSizeClass) {
        WindowSizeClass.Compact -> SmartNotebookLayout(
            notebook = notebook,
            bookId = bookId,
            stateHolder = stateHolder,
            selectedNoteIdFromRoute = selectedNoteId,
            onNavigate = onNavigate,
            onBack = onBack,
            minColumnSize = 200.dp
        )
        WindowSizeClass.Medium,
        WindowSizeClass.Expanded -> SmartNotebookLayout(
            notebook = notebook,
            bookId = bookId,
            stateHolder = stateHolder,
            selectedNoteIdFromRoute = selectedNoteId,
            onNavigate = onNavigate,
            onBack = onBack,
            minColumnSize = 200.dp
        )
    }
}

@Composable
private fun SmartNotebookLayout(
    notebook: SmartNotebook?,
    bookId: Long?,
    stateHolder: SmartNotebookStateHolder?,
    selectedNoteIdFromRoute: Long?,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit,
    minColumnSize: androidx.compose.ui.unit.Dp
) {
    val title = notebook?.smartBook?.title?.takeIf { it.isNotBlank() } ?: "New notebook"
    var pendingOpenNewNote by remember { mutableStateOf(false) }
    val notes = notebook?.atomicNotes ?: emptyList()
    val pageCount = maxOf(1, notes.size)
    var currentPage by remember(notebook?.smartBook?.bookId) { mutableIntStateOf(0) }
    currentPage = currentPage.coerceIn(0, pageCount - 1)

    val selectedNoteIdFromHolder by stateHolder?.selectedNoteId?.collectAsState(initial = null) ?: remember { mutableStateOf<Long?>(null) }
    var didInitialSyncFromRoute by remember(notebook?.smartBook?.bookId) { mutableStateOf(false) }

    LaunchedEffect(notebook, selectedNoteIdFromRoute, didInitialSyncFromRoute) {
        if (notebook == null || selectedNoteIdFromRoute == null || didInitialSyncFromRoute) return@LaunchedEffect
        val index = notebook.atomicNotes.indexOfFirst { it.noteId == selectedNoteIdFromRoute }
        if (index >= 0) currentPage = index
        didInitialSyncFromRoute = true
    }

    LaunchedEffect(notebook, selectedNoteIdFromHolder) {
        if (notebook == null || selectedNoteIdFromHolder == null) return@LaunchedEffect
        val index = notebook.atomicNotes.indexOfFirst { it.noteId == selectedNoteIdFromHolder }
        if (index >= 0) currentPage = index
        stateHolder?.setSelectedNoteId(null)
    }

    LaunchedEffect(notebook, pendingOpenNewNote) {
        if (!pendingOpenNewNote || notebook == null) return@LaunchedEffect
        val last = notebook.atomicNotes.lastOrNull() ?: return@LaunchedEffect
        stateHolder?.setSelectedNoteId(last.noteId)
        onNavigate(
            Route.NoteDetail(
                bookId = notebook.smartBook.bookId,
                noteId = last.noteId,
                isHandwritten = false
            )
        )
        pendingOpenNewNote = false
    }

    Scaffold(
        topBar = {
            DesignTopAppBar(
                title = title,
                navigationIcon = { IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.bodyLarge) } },
                actions = {
                    if (notebook != null && bookId != null && bookId != -1L) {
                        IconButton(onClick = {
                            stateHolder?.addPage()
                            pendingOpenNewNote = true
                        }) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                        IconButton(onClick = { onNavigate(Route.RelatedNotes(bookId)) }) {
                            Text("Related", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notebook == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Loading…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (notebook.atomicNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DesignSpacing.sectionSpacing)
                ) {
                    Text(
                        "No notes yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PrimaryButton(
                        text = "Add note",
                        onClick = {
                            stateHolder?.addPage()
                            pendingOpenNewNote = true
                        }
                    )
                }
            }
        } else {
            val currentNote = notes.getOrNull(currentPage)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(DesignSpacing.layoutPaddingMobile)
                ) {
                    if (currentNote != null) {
                        NoteCard(
                            note = currentNote,
                            bookId = notebook.smartBook.bookId,
                            modifier = Modifier.fillMaxSize(),
                            onClick = {
                                onNavigate(
                                    Route.NoteDetail(
                                        bookId = notebook.smartBook.bookId,
                                        noteId = currentNote.noteId,
                                        isHandwritten = currentNote.noteType == "handwritten_png"
                                    )
                                )
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignSpacing.layoutPaddingMobile)
                        .padding(top = DesignSpacing.sectionSpacing),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GhostButton(
                        text = "Prev",
                        onClick = { if (currentPage > 0) currentPage = currentPage - 1 },
                        enabled = currentPage > 0
                    )
                    Text(
                        text = "${currentPage + 1} / $pageCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    GhostButton(
                        text = "Next",
                        onClick = { if (currentPage < pageCount - 1) currentPage = currentPage + 1 },
                        enabled = currentPage < pageCount - 1
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: AtomicNote,
    bookId: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val label = when (note.noteType) {
        "text_note" -> "Text note"
        "handwritten_png" -> "Handwritten"
        else -> "New note"
    }
    DesignCard(modifier = modifier, onClick = onClick) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Note ${note.noteId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
