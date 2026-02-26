package com.originb.inkwisenote2.modules.smarthome.utils

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.originb.inkwisenote2.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * View matchers and assertions for Smart Home screen elements.
 */
object SmartHomeViewMatchers {

    /** Matcher for views that are currently visible (VISIBLE and have non-zero size). */
    fun isVisible(): Matcher<View> = isDisplayed()

    /**
     * Asserts the recent notebooks "take notes" empty-state prompt is visible.
     */
    fun takeNotesPromptId(): Int = R.id.take_notes_prompt

    /**
     * Asserts the "Created by user" label is visible (when notebooks exist).
     */
    fun createdByUserTextId(): Int = R.id.created_by_user_text

    /**
     * Asserts the "Open all" notebooks button is visible.
     */
    fun openAllNotebooksId(): Int = R.id.open_all_notebooks

    /**
     * Asserts the standing queries CTA message is visible.
     */
    fun addStandingQueriesMsgId(): Int = R.id.add_standing_queries_msg

    /**
     * Asserts the "Create Standing Query" button is visible.
     */
    fun createStandingQueryBtnId(): Int = R.id.create_standing_query_btn

    /**
     * Asserts the "Queried notes" section label is visible.
     */
    fun queriedNotesTextId(): Int = R.id.queried_notes_text
}
