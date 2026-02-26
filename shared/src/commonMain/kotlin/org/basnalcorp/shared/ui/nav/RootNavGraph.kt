package org.basnalcorp.shared.ui.nav

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.screen.PlaceholderHomeScreen
import org.basnalcorp.shared.ui.theme.ThemeId
import org.basnalcorp.shared.ui.theme.ThemeRegistry

/**
 * Root navigation graph: applies theme and renders the current screen by [Route].
 * Host (Phase 5/6) provides [context], [currentRoute], and [onNavigate];
 * shared code does not manage back stack.
 */
@Composable
fun RootNavGraph(
    context: LayoutContext,
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
    themeId: ThemeId = ThemeId.Light
) {
    val colorScheme = ThemeRegistry.get(themeId)
    MaterialTheme(colorScheme = colorScheme) {
        when (currentRoute) {
            is Route.Home -> PlaceholderHomeScreen(
                context = context,
                onNavigate = onNavigate
            )
        }
    }
}
