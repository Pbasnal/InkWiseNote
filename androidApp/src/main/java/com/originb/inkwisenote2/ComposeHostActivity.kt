package com.originb.inkwisenote2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.basnalcorp.shared.state.NotebookListStateHolder
import org.basnalcorp.shared.state.QueryListStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.Platform
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.nav.RootNavGraph
import org.basnalcorp.shared.ui.theme.ThemeId
import org.basnalcorp.shared.ui.windowSizeClassFromWidth
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
        setContent {
            var backStack by remember { mutableStateOf(listOf<Route>(Route.Home)) }
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
                    onThemeToggle = { themeId = if (themeId == ThemeId.Light) ThemeId.Dark else ThemeId.Light }
                )
            }
        }
    }
}
