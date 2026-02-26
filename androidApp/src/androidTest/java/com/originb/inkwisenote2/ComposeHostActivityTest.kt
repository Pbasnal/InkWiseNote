package com.originb.inkwisenote2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 10.2: Instrumented test for Compose host (shared UI).
 * Asserts notebook list screen is shown when app launches.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class ComposeHostActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComposeHostActivity>()

    @Test
    fun launch_showsNotebooksTitle() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Notebooks").assertIsDisplayed()
    }
}
