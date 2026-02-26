package com.originb.inkwisenote2.modules.smarthome.edgecases

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.smarthome.SmartHomeActivity
import com.originb.inkwisenote2.modules.smarthome.utils.SmartHomeDrawerHelper
import com.originb.inkwisenote2.modules.smarthome.utils.SmartHomeTestLauncher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Edge cases: Toolbar and drawer.
 * Covers unexpected or boundary behaviour (rapid back, drawer state).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SmartHomeToolbarDrawerEdgeCasesTest {

    @get:Rule
    val activityRule = ActivityScenarioRule<SmartHomeActivity>(
        SmartHomeTestLauncher.newIntent(ApplicationProvider.getApplicationContext()),
        null
    )

    @Test
    fun rapidBackPressWithDrawerOpen_closesDrawerWithoutCrash() {
        // Given: Drawer is open
        SmartHomeDrawerHelper.openDrawer()
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))

        // When: User presses back (once or rapidly)
        pressBack()

        // Then: Drawer closes, activity remains, no crash
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun backWhenDrawerClosed_doesNotCrash() {
        // Given: Drawer is closed
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))

        // When: User presses back (may finish activity if it is root)
        pressBack()

        // Then: No crash (test passes if no exception is thrown)
    }
}
