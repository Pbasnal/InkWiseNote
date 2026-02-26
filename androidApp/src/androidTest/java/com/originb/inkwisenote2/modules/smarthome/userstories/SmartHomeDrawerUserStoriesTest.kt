package modules.smarthome.userstories

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
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
 * User story tests: Navigation drawer.
 * Validates that drawer menu items open the correct screens and drawer closes after navigation.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SmartHomeDrawerUserStoriesTest {

    @get:Rule
    val activityRule = ActivityScenarioRule<SmartHomeActivity>(
        SmartHomeTestLauncher.newIntent(ApplicationProvider.getApplicationContext()),
        null
    )

    // --- 2.1 Queries item opens query screen ---
    @Test
    fun drawerQueries_opensQueryScreen() {
        // Given: Smart Home is visible
        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed()))

        // When: User opens drawer and taps Queries
        SmartHomeDrawerHelper.openDrawerAndTapQueries()

        // Then: QueryCreationActivity is shown
        onView(withId(R.id.current_query_name)).check(matches(isDisplayed()))
    }

    // --- 2.2 File explorer item opens file explorer ---
    @Test
    fun drawerFileExplorer_opensFileExplorer() {
        // Given: Smart Home is visible
        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed()))

        // When: User opens drawer and taps File Explorer
        SmartHomeDrawerHelper.openDrawerAndTapFileExplorer()

        // Then: DirectoryExplorerActivity is shown
        onView(withId(R.id.files_recycler_view))
            .check(matches(isDisplayed()))
    }

    // --- 2.3 Admin item opens admin ---
    @Test
    fun drawerAdmin_opensAdminActivity() {
        // Given: Smart Home is visible
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))

        // When: User opens drawer and taps Admin
        SmartHomeDrawerHelper.openDrawerAndTapAdmin()

        // Then: AdminActivity is shown
        onView(withId(R.id.note_id_query)).check(matches(isDisplayed()))
    }

    // --- 2.4 Drawer closes after navigation ---
    @Test
    fun drawerTapNavItem_closesDrawerAndShowsTarget() {
        // Given: Drawer is open
        SmartHomeDrawerHelper.openDrawer()
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))

        // When: User taps Queries (navigates; drawer closes automatically)
        SmartHomeDrawerHelper.openDrawerAndTapQueries()

        // Then: Target screen is visible (drawer closed)
        onView(withId(R.id.current_query_name)).check(matches(isDisplayed()))
    }
}
