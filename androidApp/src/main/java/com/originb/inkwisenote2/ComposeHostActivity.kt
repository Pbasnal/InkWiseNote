package com.originb.inkwisenote2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import org.basnalcorp.shared.state.FileExplorerStateHolder
import org.basnalcorp.shared.state.NoteDetailStateHolder
import org.basnalcorp.shared.state.NotebookListStateHolder
import org.basnalcorp.shared.state.QueryListStateHolder
import org.basnalcorp.shared.state.RelatedNotesStateHolder
import org.basnalcorp.shared.state.SmartNotebookStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.Platform
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.nav.RootNavGraph
import com.originb.inkwisenote2.common.ComposeRouteExtras
import com.originb.inkwisenote2.compose.HandwrittenNoteContentAndroid
import org.basnalcorp.shared.ui.theme.ThemeId
import org.basnalcorp.shared.ui.windowSizeClassFromWidth
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCore
import org.basnalcorp.shared.systems.markdownnote.MarkdownNoteSystem
import org.koin.java.KoinJavaComponent.get

/**
 * Single Compose Activity hosting shared RootNavGraph (Phase 5.4 / 6.2).
 * Phase 7.2: Back stack in state; onBack pops; all navigation via onNavigate.
 */
class ComposeHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notebookListStateHolder = get<NotebookListStateHolder>(NotebookListStateHolder::class.java)
        val queryListStateHolder = get<QueryListStateHolder>(QueryListStateHolder::class.java)
        val smartNotebookStateHolder = get<SmartNotebookStateHolder>(SmartNotebookStateHolder::class.java)
        val noteDetailStateHolder = get<NoteDetailStateHolder>(NoteDetailStateHolder::class.java)
        val fileExplorerStateHolder = get<FileExplorerStateHolder>(FileExplorerStateHolder::class.java)
        val relatedNotesStateHolder = get<RelatedNotesStateHolder>(RelatedNotesStateHolder::class.java)
        val chronicleCore = get<ChronicleCore>(ChronicleCore::class.java)
        val markdownNoteSystem = get<MarkdownNoteSystem>(MarkdownNoteSystem::class.java)
        val initialStack = intentToRouteStack(intent)
        setContent {
            val androidContext = LocalContext.current
            var backStack by remember { mutableStateOf(initialStack) }
            val currentRoute = backStack.last()
            var themeId by remember { mutableStateOf(ThemeId.Light) }
            BackHandler(enabled = backStack.size > 1) {
                backStack = backStack.dropLast(1)
            }
            BoxWithConstraints {
                val context = LayoutContext(
                    platform = Platform.Android,
                    windowSizeClass = windowSizeClassFromWidth(maxWidth)
                )
                RootNavGraph(
                    context = context,
                    currentRoute = currentRoute,
                    onNavigate = { backStack = backStack + it },
                    onBack = { if (backStack.size > 1) backStack = backStack.dropLast(1) },
                    themeId = themeId,
                    notebookListStateHolder = notebookListStateHolder,
                    queryListStateHolder = queryListStateHolder,
                    smartNotebookStateHolder = smartNotebookStateHolder,
                    noteDetailStateHolder = noteDetailStateHolder,
                    fileExplorerStateHolder = fileExplorerStateHolder,
                    relatedNotesStateHolder = relatedNotesStateHolder,
                    onThemeToggle = { themeId = if (themeId == ThemeId.Light) ThemeId.Dark else ThemeId.Light },
                    onShowToast = { msg -> Toast.makeText(androidContext, msg, Toast.LENGTH_SHORT).show() },
                    handwrittenNoteContent = { modifier, atomicNote, bookId ->
                        HandwrittenNoteContentAndroid(
                            modifier = modifier,
                            note = atomicNote,
                            bookId = bookId
                        )
                    },
                    chronicleCore = chronicleCore,
                    markdownNoteSystem = markdownNoteSystem
                )
            }
        }
    }

    private fun intentToRouteStack(intent: android.content.Intent): List<Route> {
        when (intent.getStringExtra(ComposeRouteExtras.ROUTE)) {
            ComposeRouteExtras.ROUTE_FILE_EXPLORER -> {
                val initialPath = intent.getStringExtra(ComposeRouteExtras.FILE_EXPLORER_INITIAL_PATH)
                return listOf(Route.Home, Route.FileExplorer(initialPath = initialPath))
            }
            ComposeRouteExtras.ROUTE_RELATED_NOTES -> {
                val bookId = intent.getLongExtra(ComposeRouteExtras.BOOK_ID, -1L)
                return listOf(Route.Home, Route.RelatedNotes(bookId = if (bookId != -1L) bookId else 0L))
            }
            ComposeRouteExtras.ROUTE_SMART_NOTEBOOK -> {
                val bookId = intent.getLongExtra(ComposeRouteExtras.BOOK_ID, -1L)
                val workingPath = intent.getStringExtra(ComposeRouteExtras.WORKING_PATH)
                if (bookId == -1L && workingPath != null) {
                    return listOf(Route.Home, Route.InitNote(workingPath = workingPath))
                }
                val route = Route.SmartNotebook(
                    bookId = if (bookId != -1L) bookId else null,
                    workingPath = workingPath,
                    bookTitle = intent.getStringExtra(ComposeRouteExtras.BOOK_TITLE),
                    noteIds = intent.getStringExtra(ComposeRouteExtras.NOTE_IDS),
                    selectedNoteId = intent.getLongExtra(ComposeRouteExtras.SELECTED_NOTE_ID, -1L).takeIf { it != -1L }
                )
                return listOf(Route.Home, route)
            }
            else -> return listOf(Route.Home)
        }
    }
}
