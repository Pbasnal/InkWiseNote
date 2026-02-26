package com.originb.inkwisenote2.modules.smarthome.utils

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.originb.inkwisenote2.R

/**
 * Espresso helpers for RecyclerViews on the Smart Home screen.
 */
object SmartHomeRecyclerViewHelper {

    /**
     * Performs a click on the item at [position] in the recent notebooks horizontal list.
     */
    fun clickRecentNotebookAt(position: Int) {
        onView(withId(R.id.user_created_notebooks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(position, click()))
    }

    /**
     * Performs a click on the item at [position] in the queried notes vertical list
     * (top-level query sections). Use for tapping a section row (e.g. toggle or open).
     */
    fun clickQueriedNotesSectionAt(position: Int) {
        onView(withId(R.id.queried_notes))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(position, click()))
    }

    /**
     * Scrolls so the item at [position] in the queried notes list is in view.
     */
    fun scrollQueriedNotesToPosition(position: Int) {
        onView(withId(R.id.queried_notes))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))
    }

    /**
     * Scrolls so the item at [position] in recent notebooks list is in view.
     */
    fun scrollRecentNotebooksToPosition(position: Int) {
        onView(withId(R.id.user_created_notebooks))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))
    }
}
