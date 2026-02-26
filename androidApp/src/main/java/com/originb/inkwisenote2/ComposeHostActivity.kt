package com.originb.inkwisenote2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.basnalcorp.shared.state.NotebookListStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.Platform
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.nav.RootNavGraph
import org.basnalcorp.shared.ui.theme.ThemeId
import org.basnalcorp.shared.ui.windowSizeClassFromWidth
import org.koin.java.KoinJavaComponent.get

/**
 * Single Compose Activity that hosts the shared root nav graph (Phase 5.4).
 * Phase 6.2: Passes [NotebookListStateHolder] and theme toggle to pilot screen.
 */
class ComposeHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notebookListStateHolder = get<NotebookListStateHolder>(NotebookListStateHolder::class.java)
        setContent {
            var currentRoute by remember { mutableStateOf<Route>(Route.Home) }
            var themeId by remember { mutableStateOf(ThemeId.Light) }
            BoxWithConstraints {
                val context = LayoutContext(
                    platform = Platform.Android,
                    windowSizeClass = windowSizeClassFromWidth(maxWidth)
                )
                RootNavGraph(
                    context = context,
                    currentRoute = currentRoute,
                    onNavigate = { currentRoute = it },
                    themeId = themeId,
                    notebookListStateHolder = notebookListStateHolder,
                    onThemeToggle = { themeId = if (themeId == ThemeId.Light) ThemeId.Dark else ThemeId.Light }
                )
            }
        }
    }
}
