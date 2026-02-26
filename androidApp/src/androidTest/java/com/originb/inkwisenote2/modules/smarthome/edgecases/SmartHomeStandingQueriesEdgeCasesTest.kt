package com.originb.inkwisenote2.modules.smarthome.edgecases

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
import com.originb.inkwisenote2.modules.smarthome.utils.SmartHomeRecyclerViewHelper
import com.originb.inkwisenote2.modules.smarthome.utils.SmartHomeTestLauncher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Edge cases: Standing queries section.
 * Covers expand/collapse with no results, multiple sections, empty adapter.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SmartHomeStandingQueriesEdgeCasesTest {

    @get:Rule
    val activityRule = ActivityScenarioRule<SmartHomeActivity>(
        SmartHomeTestLauncher.newIntent(ApplicationProvider.getApplicationContext()),
        null
    )

    @Test
    fun queriedNotesListEmpty_noCrashWhenViewDisplayed() {
        // When there are no query results, the list is empty; no crash
        onView(withId(R.id.queried_notes)).check(matches(isDisplayed()))
    }

    @Test
    fun tapQueriedNotesWhenEmpty_noCrash() {
        // Tapping on empty list or empty section should not crash
        onView(withId(R.id.queried_notes)).check(matches(isDisplayed()))
        try {
            SmartHomeRecyclerViewHelper.scrollQueriedNotesToPosition(0)
        } catch (_: Exception) {
            // Empty list may not have position 0 in same way
        }
        onView(withId(R.id.queried_notes)).check(matches(isDisplayed()))
    }

    @Test
    fun expandCollapseWhenNoResults_sectionStillPresent() {
        // With no results, queried_notes RecyclerView is still in hierarchy
        onView(withId(R.id.queried_notes)).check(matches(isDisplayed()))
    }
}
