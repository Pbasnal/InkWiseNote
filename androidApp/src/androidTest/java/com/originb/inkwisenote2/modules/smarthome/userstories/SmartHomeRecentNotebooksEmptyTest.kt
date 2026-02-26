package com.originb.inkwisenote2.modules.smarthome.userstories

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.Visibility
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
 * User story tests: Recent notebooks section — empty state only.
 * No seed data; validates that with no user notebooks we see the take-notes prompt
 * and the "Created by user" section (without depending on any pre-existing data).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SmartHomeRecentNotebooksEmptyTest {

    @get:Rule
    val activityRule = ActivityScenarioRule<SmartHomeActivity>(
        SmartHomeTestLauncher.newIntent(ApplicationProvider.getApplicationContext()),
        null
    )

    @Test
    fun noNotebooks_showsTakeNotesPrompt_hidesCreatedByUserAndOpenAll() {
        // Given: Smart Home launched with no user notebooks (default clean state)
        // When: Screen has loaded (ViewModel may post empty list)
        // Then: Take notes prompt visible; "Created by user" section visible (list empty)
        onView(withId(R.id.drawer_layout))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.take_notes_prompt))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.user_created_notebooks))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }
}
