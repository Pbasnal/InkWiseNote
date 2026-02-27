package com.originb.inkwisenote2.desktop

import androidx.compose.ui.unit.dp
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.WindowSizeClass
import org.basnalcorp.shared.ui.windowSizeClassFromWidth
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Phase 10.3: Minimal desktop unit test using shared UI types.
 */
class SharedUiTest {

    @Test
    fun routeHome_exists() {
        val home = Route.Home
        assertEquals(Route.Home, home)
    }

    @Test
    fun windowSizeClassFromWidth_narrow_returnsCompact() {
        assertEquals(WindowSizeClass.Compact, windowSizeClassFromWidth(400.dp))
    }

    @Test
    fun windowSizeClassFromWidth_medium_returnsMedium() {
        assertEquals(WindowSizeClass.Medium, windowSizeClassFromWidth(700.dp))
    }

    @Test
    fun windowSizeClassFromWidth_wide_returnsExpanded() {
        assertEquals(WindowSizeClass.Expanded, windowSizeClassFromWidth(1000.dp))
    }
}
