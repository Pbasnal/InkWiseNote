package org.basnalcorp.shared.ui.nav

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.basnalcorp.shared.state.NotebookListStateHolder
import org.basnalcorp.shared.state.QueryListStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.screen.AdminScreen
import org.basnalcorp.shared.ui.screen.FileExplorerScreen
import org.basnalcorp.shared.ui.screen.NoteDetailScreen
import org.basnalcorp.shared.ui.screen.NotebookListScreen
import org.basnalcorp.shared.ui.screen.QueryCreationScreen
import org.basnalcorp.shared.ui.screen.QueryListScreen
import org.basnalcorp.shared.ui.screen.QueryResultsScreen
import org.basnalcorp.shared.ui.screen.RelatedNotesScreen
import org.basnalcorp.shared.ui.screen.SearchScreen
import org.basnalcorp.shared.ui.screen.SmartNotebookScreen
import org.basnalcorp.shared.ui.theme.ThemeId
import org.basnalcorp.shared.ui.theme.ThemeRegistry

/**
 * Root navigation graph (Phase 7.1/7.2): applies theme and renders current screen by [Route].
 * Host provides [context], [currentRoute], [onNavigate], [onBack], and optional state holders.
 */
@Composable
fun RootNavGraph(
    context: LayoutContext,
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit,
    themeId: ThemeId = ThemeId.Light,
    notebookListStateHolder: NotebookListStateHolder? = null,
    queryListStateHolder: QueryListStateHolder? = null,
    onThemeToggle: (() -> Unit)? = null
) {
    val colorScheme = ThemeRegistry.get(themeId)
    MaterialTheme(colorScheme = colorScheme) {
        when (val route = currentRoute) {
            is Route.Home -> NotebookListScreen(
                context = context,
                stateHolder = notebookListStateHolder,
                onNavigate = onNavigate,
                onThemeToggle = onThemeToggle
            )
            is Route.Search -> SearchScreen(context = context, onNavigate = onNavigate, onBack = onBack)
            is Route.QueryList -> QueryListScreen(
                context = context,
                stateHolder = queryListStateHolder,
                onNavigate = onNavigate,
                onBack = onBack
            )
            is Route.QueryResults -> QueryResultsScreen(
                context = context,
                queryName = route.queryName,
                onNavigate = onNavigate,
                onBack = onBack
            )
            is Route.QueryCreation -> QueryCreationScreen(context = context, onNavigate = onNavigate, onBack = onBack)
            is Route.SmartNotebook -> SmartNotebookScreen(
                context = context,
                bookId = route.bookId,
                workingPath = route.workingPath,
                bookTitle = route.bookTitle,
                selectedNoteId = route.selectedNoteId,
                onNavigate = onNavigate,
                onBack = onBack
            )
            is Route.NoteDetail -> NoteDetailScreen(
                context = context,
                bookId = route.bookId,
                noteId = route.noteId,
                isHandwritten = route.isHandwritten,
                onNavigate = onNavigate,
                onBack = onBack
            )
            is Route.Admin -> AdminScreen(context = context, onNavigate = onNavigate, onBack = onBack)
            is Route.FileExplorer -> FileExplorerScreen(
                context = context,
                initialPath = route.initialPath,
                onNavigate = onNavigate,
                onBack = onBack
            )
            is Route.RelatedNotes -> RelatedNotesScreen(
                context = context,
                bookId = route.bookId,
                onNavigate = onNavigate,
                onBack = onBack
            )
        }
    }
}
