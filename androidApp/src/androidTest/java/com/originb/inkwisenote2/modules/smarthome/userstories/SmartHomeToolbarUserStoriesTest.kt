package com.originb.inkwisenote2.modules.smarthome.userstories

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.Matchers.allOf
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
 * User story tests: Toolbar and global actions.
 * Validates features directly used by the user (search, FAB, drawer, back).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SmartHomeToolbarUserStoriesTest {

    @get:Rule
    val activityRule = ActivityScenarioRule<SmartHomeActivity>(
        SmartHomeTestLauncher.newIntent(ApplicationProvider.getApplicationContext()),
        null
    )

    // --- 1.1 Search opens search screen ---
    @Test
    fun searchButton_opensSearchScreen() {
        // Given: Smart Home is visible
        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed()))

        // When: User taps search button (disambiguate: two views share search_button id - use contentDescription)
        onView(allOf(withId(R.id.search_button), withContentDescription("search")))
            .perform(androidx.test.espresso.action.ViewActions.click())

        // Then: NoteSearchActivity is shown (search input visible)
        onView(withId(R.id.searchInput))
            .check(matches(isDisplayed()))
    }

    // --- 1.2 FAB opens new note ---
    @Test
    fun fab_opensNewNote() {
        // Given: Smart Home is visible
        onView(withId(R.id.add_new_note_btn))
            .check(matches(isDisplayed()))

        // When: User taps FAB
        onView(withId(R.id.add_new_note_btn))
            .perform(androidx.test.espresso.action.ViewActions.click())

        // Then: SmartNotebookActivity is shown (drawing/smart note view visible)
        onView(withId(com.originb.inkwisenote2.R.id.tap_to_text))
            .check(matches(isDisplayed()))
        onView(withId(com.originb.inkwisenote2.R.id.touch_to_write))
            .check(matches(isDisplayed()))
    }

    // --- 1.3 Drawer opens from toolbar ---
    @Test
    fun drawerToggle_opensDrawer() {
        // Given: Smart Home is visible
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))

        // When: User opens the drawer (via DrawerLayout)
        SmartHomeDrawerHelper.openDrawer()

        // Then: Navigation view is visible (drawer content)
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
    }

    // --- 1.4 Back with drawer open closes drawer ---
    @Test
    fun backWithDrawerOpen_closesDrawerActivityRemains() {
        // Given: Drawer is open
        SmartHomeDrawerHelper.openDrawer()
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))

        // When: User presses back
        androidx.test.espresso.Espresso.pressBack()

        // Then: Drawer closes, Smart Home remains (main content visible)
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
    }
}
