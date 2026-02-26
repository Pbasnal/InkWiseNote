package com.originb.inkwisenote2.modules.smarthome.utils

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import com.originb.inkwisenote2.modules.smarthome.SmartHomeActivity

/**
 * Launches [SmartHomeActivity] in a clean state (new task, clear task).
 * Use for tests that need a fresh home screen without existing activity stack.
 */
object SmartHomeTestLauncher {

    /**
     * Intent flags used by the app for "start fresh" home.
     */
    const val FLAGS_FRESH_HOME = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

    /**
     * Builds an intent to launch Smart Home as the only activity in the task.
     */
    fun newIntent(context: Context): Intent =
        Intent(context, SmartHomeActivity::class.java).apply {
            addFlags(FLAGS_FRESH_HOME)
        }

    /**
     * Launches Smart Home with a clean task. Use when you need a fresh stack (e.g. in @Before).
     */
    fun launchSmartHomeFresh(context: Context): ActivityScenario<SmartHomeActivity> =
        ActivityScenario.launch(newIntent(context))
}
