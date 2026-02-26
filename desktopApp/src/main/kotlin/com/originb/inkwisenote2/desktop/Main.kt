package com.originb.inkwisenote2.desktop

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.Platform
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.nav.RootNavGraph
import org.basnalcorp.shared.ui.theme.ThemeId
import org.basnalcorp.shared.ui.windowSizeClassFromWidth

/**
 * Phase 6.3: Desktop shows same shared RootNavGraph and pilot NotebookListScreen.
 * LayoutContext uses Platform.Desktop; notebook list state is null until Phase 8 (Koin/actuals).
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "InkWiseNote"
    ) {
        var themeId by remember { mutableStateOf(ThemeId.Light) }
        BoxWithConstraints {
            val context = LayoutContext(
                platform = Platform.Desktop,
                windowSizeClass = windowSizeClassFromWidth(maxWidth)
            )
            RootNavGraph(
                context = context,
                currentRoute = Route.Home,
                onNavigate = { },
                themeId = themeId,
                notebookListStateHolder = null,
                onThemeToggle = { themeId = if (themeId == ThemeId.Light) ThemeId.Dark else ThemeId.Light }
            )
        }
    }
}
