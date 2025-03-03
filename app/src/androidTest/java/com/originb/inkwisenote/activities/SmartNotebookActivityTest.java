package com.originb.inkwisenote.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.Gravity;
import android.widget.Toast;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.originb.inkwisenote.R;
import com.originb.inkwisenote.common.BitmapScale;
import com.originb.inkwisenote.modules.handwrittennotes.data.HandwrittenNoteEntity;
import com.originb.inkwisenote.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote.modules.handwrittennotes.data.HandwrittenNoteWithImage;
import com.originb.inkwisenote.modules.repositories.*;
import com.originb.inkwisenote.utils.DrawingTestUtils;
import com.originb.inkwisenote.AppMainActivity;
import com.originb.inkwisenote.modules.smartnotes.ui.SmartNotebookActivity;
import com.originb.inkwisenote.testutils.HandwritingStrokeHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SmartNotebookActivityTest {

    @Rule
    public ActivityScenarioRule<AppMainActivity> bootStrapActivity =
            new ActivityScenarioRule<>(AppMainActivity.class);

    @Rule
    public ActivityScenarioRule<SmartNotebookActivity> activityRule =
            new ActivityScenarioRule<>(SmartNotebookActivity.class);

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
    public void testDrawingStrokes() {
        showTestStep("Testing Drawing Strokes");
        // Draw multiple strokes
        onView(withId(R.id.smart_drawing_view))
                .perform(DrawingTestUtils.drawStroke("Drawing straight line",
                        DrawingTestUtils.createStraightLine()));

        waitFor(500);

        onView(withId(R.id.smart_drawing_view))
                .perform(DrawingTestUtils.drawStroke("Drawing zigzag",
                        DrawingTestUtils.createZigzagLine()));

        waitFor(500);

        onView(withId(R.id.smart_drawing_view))
                .perform(DrawingTestUtils.drawStroke("Drawing circle",
                        DrawingTestUtils.createCircle()));

        waitFor(2000);
    }

    @Test
    public void testWritingHelloWorld() {
        showTestStep("Writing 'Hello World!'");
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Repositories.registerRepositories(context);

        List<List<HandwritingStrokeHelper.Point>> allStrokes = HandwritingStrokeHelper.getHelloWorldStrokes(0, 0);

        for (List<HandwritingStrokeHelper.Point> stroke : allStrokes) {
            onView(withId(R.id.smart_drawing_view))
                    .perform(DrawingTestUtils.drawStroke("Writing stroke", stroke));
        }

        pressBack();
        waitFor(1000); // Final delay to see the result

        // Now verify the data persistence
        HandwrittenNoteRepository repository = Repositories.getInstance().getHandwrittenNoteRepository();
        SmartNotebookRepository smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        List<SmartNotebook> allBooks = smartNotebookRepository.getAllSmartNotebooks();

        assertEquals(1, allBooks.size());
        assertEquals(1, allBooks.get(0).smartBookPages.size());
        assertEquals(1, allBooks.get(0).atomicNotes.size());

        AtomicNoteEntity atomicNote = allBooks.get(0).atomicNotes.get(0);


        // Verify database entry
        HandwrittenNoteWithImage savedNote = repository.getNoteImage(atomicNote, BitmapScale.FULL_SIZE);
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

    // Custom ViewAction to draw strokes


    // Helper methods to create different stroke patterns

} 