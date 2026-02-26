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

// --- SmartNotebookScreen (notebook detail with pages) ---

@Composable
fun SmartNotebookScreen(
    context: LayoutContext,
    bookId: Long?,
    workingPath: String?,
    bookTitle: String?,
    selectedNoteId: Long?,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    when (context.windowSizeClass) {
        WindowSizeClass.Compact -> StubLayout("Notebook", "bookId=$bookId", onBack)
        WindowSizeClass.Medium, WindowSizeClass.Expanded -> StubLayout("Notebook", "bookId=$bookId", onBack)
    }
}

// --- NoteDetailScreen (text or handwritten note) ---

@Composable
fun NoteDetailScreen(
    context: LayoutContext,
    bookId: Long,
    noteId: Long,
    isHandwritten: Boolean,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    when (context.windowSizeClass) {
        WindowSizeClass.Compact -> StubLayout("Note", "noteId=$noteId", onBack)
        WindowSizeClass.Medium, WindowSizeClass.Expanded -> StubLayout("Note", "noteId=$noteId", onBack)
    }
}

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

// --- FileExplorerScreen ---

@Composable
fun FileExplorerScreen(
    context: LayoutContext,
    initialPath: String?,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    when (context.windowSizeClass) {
        WindowSizeClass.Compact -> StubLayout("File explorer", initialPath ?: "TBD", onBack)
        WindowSizeClass.Medium, WindowSizeClass.Expanded -> StubLayout("File explorer", initialPath ?: "TBD", onBack)
    }
}

// --- RelatedNotesScreen ---

@Composable
fun RelatedNotesScreen(
    context: LayoutContext,
    bookId: Long,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    when (context.windowSizeClass) {
        WindowSizeClass.Compact -> StubLayout("Related notes", "bookId=$bookId", onBack)
        WindowSizeClass.Medium, WindowSizeClass.Expanded -> StubLayout("Related notes", "bookId=$bookId", onBack)
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
