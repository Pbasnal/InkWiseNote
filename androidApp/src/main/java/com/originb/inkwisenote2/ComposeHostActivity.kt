package com.originb.inkwisenote2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.Platform
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.nav.RootNavGraph
import org.basnalcorp.shared.ui.theme.ThemeId
import org.basnalcorp.shared.ui.windowSizeClassFromWidth

/**
 * Single Compose Activity that hosts the shared root nav graph (Phase 5.4).
 * Computes [LayoutContext] from current window size and passes [Platform.Android].
 */
class ComposeHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentRoute by remember { mutableStateOf<Route>(Route.Home) }
            BoxWithConstraints {
                val context = LayoutContext(
                    platform = Platform.Android,
                    windowSizeClass = windowSizeClassFromWidth(maxWidth)
                )
                RootNavGraph(
                    context = context,
                    currentRoute = currentRoute,
                    onNavigate = { currentRoute = it },
                    themeId = ThemeId.Light
                )
            }
        }
    }
}
