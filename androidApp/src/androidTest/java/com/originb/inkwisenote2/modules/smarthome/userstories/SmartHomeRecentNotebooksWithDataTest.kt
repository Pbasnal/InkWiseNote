package com.originb.inkwisenote2.modules.smarthome.userstories

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.smarthome.SmartHomeActivity
import com.originb.inkwisenote2.modules.smarthome.utils.SmartHomeRecyclerViewHelper
import com.originb.inkwisenote2.modules.smarthome.utils.SmartHomeSeedDataUtils
import com.originb.inkwisenote2.modules.smarthome.utils.SmartHomeTestLauncher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * User story tests: Recent notebooks section when seed data exists.
 * Seeds one notebook with a text note in @Before, cleans up in @After.
 * Uses programmatic seed so EventBus and dashboard pipeline run.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SmartHomeRecentNotebooksWithDataTest {

    @get:Rule
    val activityRule = ActivityScenarioRule<SmartHomeActivity>(
        SmartHomeTestLauncher.newIntent(ApplicationProvider.getApplicationContext()),
        null
    )

    private val createdNotebooks = mutableListOf<SmartNotebook>()

    @Before
    fun seedNotebook() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val workingPath = context.filesDir.path
        val notebook = SmartHomeSeedDataUtils.createNotebook(context, workingPath, null)
        SmartHomeSeedDataUtils.addTextNoteToNotebook(notebook, "Seed note text", context)
        createdNotebooks.add(notebook)
        // Allow EventBus and LiveData to propagate before tests run
        Thread.sleep(500)
    }

    @After
    fun deleteCreatedNotebooks() {
        SmartHomeSeedDataUtils.deleteNotebooks(createdNotebooks)
        createdNotebooks.clear()
    }

    @Test
    fun withNotebooks_showsCreatedByUserAndOpenAllAndList() {
        onView(withId(R.id.user_created_notebooks))
            .check(matches(isDisplayed()))
        onView(withId(R.id.created_by_user_text))
            .check(matches(isDisplayed()))
        onView(withId(R.id.open_all_notebooks))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openAllNotebooks_opensSearchWithAllNotebooks() {
        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed()))
        onView(withId(R.id.open_all_notebooks))
            .perform(click())
        onView(withId(R.id.searchInput))
            .check(matches(isDisplayed()))
    }

    @Test
    fun tapNotebookCard_opensNotebook() {
        onView(withId(R.id.user_created_notebooks))
            .check(matches(isDisplayed()))
        SmartHomeRecyclerViewHelper.clickRecentNotebookAt(0)
        onView(withId(R.id.smart_drawing_view))
            .check(matches(isDisplayed()))
    }
}
