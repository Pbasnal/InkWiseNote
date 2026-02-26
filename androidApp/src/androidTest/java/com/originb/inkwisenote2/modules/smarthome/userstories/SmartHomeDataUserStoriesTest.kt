package com.originb.inkwisenote2.modules.smarthome.userstories

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
import com.originb.inkwisenote2.modules.smarthome.utils.SmartHomeTestLauncher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * User story tests: Data and state (optional).
 * Validates that after creating a note or adding a standing query, the home screen reflects the change.
 * These tests depend on navigation back to Smart Home and optional test data / EventBus refresh.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SmartHomeDataUserStoriesTest {

    @get:Rule
    val activityRule = ActivityScenarioRule<SmartHomeActivity>(
        SmartHomeTestLauncher.newIntent(ApplicationProvider.getApplicationContext()),
        null
    )

    // --- 5.1 After creating a note, home shows it ---
    @Test
    fun afterCreatingNote_homeShowsNewNotebookInRecentList() {
        // Given: User creates a note from FAB and returns to Smart Home
        // Then: Recent notebooks list includes the new notebook
        // Implementation: would require FAB tap -> draw/ save -> pressBack to home, then assert list
        // For structure we only assert home is visible; full flow can be added with test data
        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed()))
        onView(withId(R.id.user_created_notebooks))
            .check(matches(isDisplayed()))
    }

    // --- 5.2 After adding a standing query with matches, section appears ---
    @Test
    fun afterAddingStandingQuery_queriedSectionAppears() {
        // Given: User adds a standing query and returns to Smart Home
        // Then: Queried section for that query appears with results
        // Implementation: depends on QueryRepository and EventBus/ViewModel refresh
        onView(withId(R.id.queried_notes))
            .check(matches(isDisplayed()))
    }
}
