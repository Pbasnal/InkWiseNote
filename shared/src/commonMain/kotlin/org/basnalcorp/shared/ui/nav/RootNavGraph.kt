package org.basnalcorp.shared.ui.nav

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.basnalcorp.shared.state.NotebookListStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.screen.NotebookListScreen
import org.basnalcorp.shared.ui.theme.ThemeId
import org.basnalcorp.shared.ui.theme.ThemeRegistry

/**
 * Root navigation graph: applies theme and renders the current screen by [Route].
 * Host (Phase 5/6) provides [context], [currentRoute], [onNavigate], and optional [notebookListStateHolder];
 * shared code does not manage back stack.
 */
@Composable
fun RootNavGraph(
    context: LayoutContext,
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
    themeId: ThemeId = ThemeId.Light,
    notebookListStateHolder: NotebookListStateHolder? = null,
    onThemeToggle: (() -> Unit)? = null
) {
    val colorScheme = ThemeRegistry.get(themeId)
    MaterialTheme(colorScheme = colorScheme) {
        when (currentRoute) {
            is Route.Home -> NotebookListScreen(
                context = context,
                stateHolder = notebookListStateHolder,
                onNavigate = onNavigate,
                onThemeToggle = onThemeToggle
            )
        }
    }
}
