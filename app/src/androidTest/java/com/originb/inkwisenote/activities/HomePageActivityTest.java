package com.originb.inkwisenote.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.Gravity;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import androidx.test.platform.app.InstrumentationRegistry;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.config.Feature;
import com.originb.inkwisenote.common.BitmapScale;
import com.originb.inkwisenote.modules.handwrittennotes.data.HandwrittenNoteEntity;
import com.originb.inkwisenote.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote.common.BitmapFileIoUtils;
import com.originb.inkwisenote.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote.modules.handwrittennotes.data.HandwrittenNoteWithImage;
import com.originb.inkwisenote.modules.repositories.*;
import com.originb.inkwisenote.testutils.HandwritingStrokeHelper;
import com.originb.inkwisenote.utils.DrawingTestUtils;
import com.originb.inkwisenote.AppMainActivity;
import com.originb.inkwisenote.HomePageActivity;

import com.originb.inkwisenote.modules.handwrittennotes.ui.DrawingView;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

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

            openSmartNotebookActivity();
            waitFor(1000);
            pressBack();
            waitFor(1000);
        }
    }

    @Test
    public void testHandwrittenNoteUpdateFeature() {
        if (!configReader.isFeatureEnabled(Feature.MARKDOWN_EDITOR) &&
                !configReader.isFeatureEnabled(Feature.CAMERA_NOTE)) return;
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Repositories.registerRepositories(context);

        openSmartNotebookActivity();
        drawHelloWorld(0, 0);
        pressBack();
        waitFor(1000); // time to save the note and home page to load

        // open the note again to update it
        onView(withId(R.id.card_image))
                .perform(click());
        waitFor(500);

        // Verify we're back in SmartNotebookActivity
        onView(withId(R.id.smart_drawing_view))
                .check(matches(isDisplayed()));

        drawHelloWorld(10, 200);
        waitFor(5000);
    }



    @Test
    public void testHandwrittenNoteSaveFeature() {
        // Only test if feature is enabled
        if (!configReader.isFeatureEnabled(Feature.MARKDOWN_EDITOR) &&
                !configReader.isFeatureEnabled(Feature.CAMERA_NOTE)) return;

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Repositories.registerRepositories(context);

        openSmartNotebookActivity();
        drawHelloWorld(0, 0);
        pressBack();
        waitFor(2000);

        // Now verify the data persistence
        HandwrittenNoteRepository repository = Repositories.getInstance().getHandwrittenNoteRepository();
        SmartNotebookRepository smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        List<SmartNotebook> allBooks = smartNotebookRepository.getAllSmartNotebooks();

        assertEquals(1, allBooks.size());
        assertEquals(1, allBooks.get(0).smartBookPages.size());
        assertEquals(1, allBooks.get(0).atomicNotes.size());

        AtomicNoteEntity atomicNote = allBooks.get(0).atomicNotes.get(0);
        HandwrittenNoteWithImage savedNote = repository.getNoteImage(atomicNote, BitmapScale.FULL_SIZE);
        validateHandwrittenNote(savedNote, atomicNote);

        onView(withId(R.id.note_card_grid_view))
                .check(matches(isDisplayed()));

        // Verify grid has exactly one item
        onView(withId(R.id.note_card_grid_view))
                .check((view, noViewFoundException) -> {
                    RecyclerView recyclerView = (RecyclerView) view;
                    RecyclerView.Adapter adapter = recyclerView.getAdapter();
                    assertEquals("Grid should have exactly one note", 1, adapter.getItemCount());
                });

        // Verify the note thumbnail is displayed
        onView(withId(R.id.card_image))
                .check(matches(isDisplayed()));

        // Verify the note can be opened again
        onView(withId(R.id.card_image))
                .perform(click());
        waitFor(500);

        // Verify we're back in SmartNotebookActivity
        onView(withId(R.id.smart_drawing_view))
                .check(matches(isDisplayed()));

        // Verify the drawing is still there
        onView(withId(R.id.smart_drawing_view))
                .check((view, noViewFoundException) -> {
                    DrawingView drawingView = (DrawingView) view;
                    Bitmap bitmap = drawingView.getBitmap();

                    boolean bitmapsAreEqual = BitmapFileIoUtils.areEqual(bitmap, savedNote.noteImage.get());
                    assertTrue("Drawing should be same as the one saved", bitmapsAreEqual);
                });
    }

    private void validateHandwrittenNote(HandwrittenNoteWithImage savedNote, AtomicNoteEntity atomicNote) {

        // Verify database entry
        assertNotNull("HandwrittenNoteWithImage should exist in database", savedNote);
        assertNotNull("HandwrittenNoteEntity should exist in database", savedNote.handwrittenNoteEntity);
        assertNotNull("Note image should get loaded", savedNote.noteImage);
        assertEquals("Note ID should match", atomicNote.getNoteId(), savedNote.handwrittenNoteEntity.getNoteId());

        // Try to load the bitmap to verify it's valid
        Optional<Bitmap> savedBitmap = savedNote.noteImage;
        assertTrue("Should be able to read saved bitmap", savedBitmap.isPresent());
        assertNotNull("Bitmap should be valid", savedBitmap.get());

        // Verify template file exists
        File templateFile = new File(savedNote.handwrittenNoteEntity.getPageTemplateFilePath());
        assertTrue("Template file should exist", templateFile.exists());
        assertTrue("Template file should not be empty", templateFile.length() > 0);


        HandwrittenNoteEntity handwrittenNoteEntity = savedNote.handwrittenNoteEntity;
        // Verify timestamps
        assertTrue("Creation time should be set", handwrittenNoteEntity.getCreatedTimeMillis() > 0);
        assertTrue("Last modified time should be set", handwrittenNoteEntity.getLastModifiedTimeMillis() > 0);
        assertTrue("Last modified time should be after creation time",
                handwrittenNoteEntity.getLastModifiedTimeMillis() >= handwrittenNoteEntity.getCreatedTimeMillis());

    }

    private void openSmartNotebookActivity() {
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
    }

    private void drawHelloWorld(float x, float y) {
        List<List<HandwritingStrokeHelper.Point>> allStrokes = HandwritingStrokeHelper.getHelloWorldStrokes(0, 0);

        for (List<HandwritingStrokeHelper.Point> stroke : allStrokes) {
            onView(withId(R.id.smart_drawing_view))
                    .perform(DrawingTestUtils.drawStroke("Writing stroke", stroke));
        }
    }
} 