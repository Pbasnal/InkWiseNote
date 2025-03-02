package com.originb.inkwisenote.activities;

import android.os.SystemClock;
import android.view.Gravity;
import android.widget.Toast;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.originb.inkwisenote.R;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.config.Feature;
import com.originb.inkwisenote.ux.activities.AppMainActivity;
import com.originb.inkwisenote.ux.activities.HomePageActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomePageActivityTest {

    @Rule
    public ActivityScenarioRule<AppMainActivity> bootStrapActivity =
            new ActivityScenarioRule<>(AppMainActivity.class);
    @Rule
    public ActivityScenarioRule<HomePageActivity> activityRule =
            new ActivityScenarioRule<>(HomePageActivity.class);

    private ConfigReader configReader;

    // Add delay for visibility
    static {
        SystemClock.sleep(500);
    }

    @Before
    public void setup() {
        ActivityScenario<HomePageActivity> scenario = activityRule.getScenario();
        scenario.onActivity(activity -> {
            configReader = ConfigReader.fromContext(activity);
        });
    }

    // Helper method to show test progress
    private void showTestStep(String step) {
        activityRule.getScenario().onActivity(activity -> {
            Toast toast = Toast.makeText(activity, "Testing: " + step, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 100);
            toast.show();
        });
    }

    // Helper method to add delays between steps
    private void waitFor(long millis) {
        SystemClock.sleep(millis);
    }

    @Test
    public void testFabMenuInteractions() {
        // Only test if feature is enabled
        if (configReader.isFeatureEnabled(Feature.MARKDOWN_EDITOR) ||
                configReader.isFeatureEnabled(Feature.CAMERA_NOTE)) {

            showTestStep("Opening FAB Menu");
            waitFor(100);

            // Test FAB menu opens
            onView(withId(R.id.new_note_opt))
                    .perform(click());
            waitFor(500);

            onView(withId(R.id.fab_menu_container))
                    .check(matches(isDisplayed()));

            showTestStep("Testing Smart Note Creation");
            onView(withId(R.id.fab_camera))
                    .perform(click());
            waitFor(500);

            // Verify SmartNotebookActivity is launched
            onView(withId(R.id.smart_note_page_view))
                    .check(matches(isDisplayed()));
            waitFor(1000);
            pressBack();
            waitFor(1000);
        }
    }
} 