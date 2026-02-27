package com.originb.inkwisenote2.desktop

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.basnalcorp.shared.di.sharedModule
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
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCore
import org.basnalcorp.shared.ui.theme.ThemeId
import org.basnalcorp.shared.ui.windowSizeClassFromWidth
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get

/**
 * Phase 6.3 / 8.1: Desktop runs shared RootNavGraph with full data (Koin + jvmMain actuals).
 */
fun main() {
    startKoin {
        modules(desktopActualsModule, sharedModule())
    }
    application {
        val notebookListStateHolder = get<NotebookListStateHolder>(NotebookListStateHolder::class.java)
        val queryListStateHolder = get<QueryListStateHolder>(QueryListStateHolder::class.java)
        val smartNotebookStateHolder = get<SmartNotebookStateHolder>(SmartNotebookStateHolder::class.java)
        val noteDetailStateHolder = get<NoteDetailStateHolder>(NoteDetailStateHolder::class.java)
        val fileExplorerStateHolder = get<FileExplorerStateHolder>(FileExplorerStateHolder::class.java)
        val relatedNotesStateHolder = get<RelatedNotesStateHolder>(RelatedNotesStateHolder::class.java)
        Window(
            onCloseRequest = ::exitApplication,
            title = "InkWiseNote"
        ) {
            val chronicleCore = get<ChronicleCore>(ChronicleCore::class.java)
            var backStack by remember { mutableStateOf(listOf<Route>(Route.Home)) }
            val currentRoute = backStack.last()
            var themeId by remember { mutableStateOf(ThemeId.Light) }
            BoxWithConstraints {
                val context = LayoutContext(
                    platform = Platform.Desktop,
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
                    onShowToast = { msg -> println(msg) },
                    chronicleCore = chronicleCore
                )
            }
        }
    }
}
