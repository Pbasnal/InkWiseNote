package org.basnalcorp.shared.ui.nav

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.basnalcorp.shared.state.FileExplorerStateHolder
import org.basnalcorp.shared.state.NoteDetailStateHolder
import org.basnalcorp.shared.state.NotebookListStateHolder
import org.basnalcorp.shared.state.QueryListStateHolder
import org.basnalcorp.shared.state.RelatedNotesStateHolder
import org.basnalcorp.shared.state.SmartNotebookStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.screen.AdminScreen
import org.basnalcorp.shared.ui.screen.ChronicleNoteDetailScreen
import org.basnalcorp.shared.ui.screen.ChronicleTestScreen
import org.basnalcorp.shared.ui.screen.FileExplorerScreen
import org.basnalcorp.shared.ui.screen.InitNoteScreen
import org.basnalcorp.shared.ui.screen.NoteDetailScreen
import org.basnalcorp.shared.ui.screen.NotebookListScreen
import org.basnalcorp.shared.ui.screen.QueryCreationScreen
import org.basnalcorp.shared.ui.screen.QueryListScreen
import org.basnalcorp.shared.ui.screen.QueryResultsScreen
import org.basnalcorp.shared.ui.screen.RelatedNotesScreen
import org.basnalcorp.shared.ui.screen.SearchScreen
import org.basnalcorp.shared.ui.screen.SmartNotebookScreen
import org.basnalcorp.shared.domain.AtomicNote
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCore
import org.basnalcorp.shared.systems.markdownnote.MarkdownNoteSystem
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
    smartNotebookStateHolder: SmartNotebookStateHolder? = null,
    noteDetailStateHolder: NoteDetailStateHolder? = null,
    fileExplorerStateHolder: FileExplorerStateHolder? = null,
    relatedNotesStateHolder: RelatedNotesStateHolder? = null,
    onThemeToggle: (() -> Unit)? = null,
    onShowToast: ((String) -> Unit)? = null,
    handwrittenNoteContent: (@Composable (Modifier, AtomicNote, Long) -> Unit)? = null,
    chronicleCore: ChronicleCore? = null,
    markdownNoteSystem: MarkdownNoteSystem? = null
) {
    val theme = ThemeRegistry.get(themeId)
    MaterialTheme(colorScheme = theme.colorScheme, typography = theme.typography) {
        when (val route = currentRoute) {
            is Route.Home -> NotebookListScreen(
                context = context,
                stateHolder = notebookListStateHolder,
                chronicleCore = chronicleCore,
                onNavigate = onNavigate,
                onThemeToggle = onThemeToggle,
                onShowToast = onShowToast
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
            is Route.InitNote -> InitNoteScreen(
                context = context,
                workingPath = route.workingPath,
                chronicleNotebookId = route.chronicleNotebookId,
                stateHolder = smartNotebookStateHolder,
                noteDetailStateHolder = noteDetailStateHolder,
                chronicleCore = chronicleCore,
                markdownNoteSystem = markdownNoteSystem,
                onNavigate = onNavigate,
                onBack = onBack,
                onShowToast = onShowToast
            )
            is Route.SmartNotebook -> SmartNotebookScreen(
                context = context,
                stateHolder = smartNotebookStateHolder,
                bookId = route.bookId,
                workingPath = route.workingPath,
                bookTitle = route.bookTitle,
                selectedNoteId = route.selectedNoteId,
                onNavigate = onNavigate,
                onBack = onBack
            )
            is Route.NoteDetail -> NoteDetailScreen(
                context = context,
                stateHolder = noteDetailStateHolder,
                bookId = route.bookId,
                noteId = route.noteId,
                isHandwritten = route.isHandwritten,
                onNavigate = onNavigate,
                onBack = onBack,
                onShowToast = onShowToast,
                handwrittenContent = handwrittenNoteContent
            )
            is Route.Admin -> AdminScreen(context = context, onNavigate = onNavigate, onBack = onBack)
            is Route.FileExplorer -> FileExplorerScreen(
                context = context,
                stateHolder = fileExplorerStateHolder,
                initialPath = route.initialPath,
                onNavigate = onNavigate,
                onBack = onBack
            )
            is Route.RelatedNotes -> RelatedNotesScreen(
                context = context,
                stateHolder = relatedNotesStateHolder,
                bookId = route.bookId,
                onNavigate = onNavigate,
                onBack = onBack
            )
            is Route.ChronicleTest -> ChronicleTestScreen(
                context = context,
                chronicleCore = chronicleCore,
                onBack = onBack,
                onNavigate = onNavigate,
                onShowToast = onShowToast
            )
            is Route.ChronicleNoteDetail -> ChronicleNoteDetailScreen(
                context = context,
                notebookId = route.notebookId,
                noteId = route.noteId,
                markdownNoteSystem = markdownNoteSystem,
                onBack = onBack,
                onNavigate = onNavigate,
                onShowToast = onShowToast
            )
        }
    }
}
