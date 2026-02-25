package com.originb.inkwisenote2.activities

import android.graphics.Bitmap
import android.os.SystemClock
import android.view.Gravity
import android.widget.Toast
import androidx.test.core.app.ActivityScenario.ActivityAction
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.originb.inkwisenote2.AppMainActivity
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.BitmapScale
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteEntity
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNotebookActivity
import com.originb.inkwisenote2.testutils.HandwritingStrokeHelper
import com.originb.inkwisenote2.utils.DrawingTestUtils
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.get
import java.io.File

@RunWith(AndroidJUnit4::class)
@LargeTest
class SmartNotebookActivityTest {
    @Rule
    var bootStrapActivity: ActivityScenarioRule<AppMainActivity> =
        ActivityScenarioRule<AppMainActivity>(AppMainActivity::class.java)

    @Rule
    var activityRule: ActivityScenarioRule<SmartNotebookActivity> = ActivityScenarioRule<SmartNotebookActivity>(
        SmartNotebookActivity::class.java
    )

    // Helper method to show test progress
    private fun showTestStep(step: String?) {
        activityRule.getScenario().onActivity(ActivityAction { activity: SmartNotebookActivity? ->
            val toast = Toast.makeText(activity, "Testing: $step", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP or Gravity.CENTER, 0, 100)
            toast.show()
        })
    }

    // Helper method to add delays between steps
    private fun waitFor(millis: Long) {
        SystemClock.sleep(millis)
    }

    @Test
    fun testDrawingStrokes() {
        showTestStep("Testing Drawing Strokes")
        // Draw multiple strokes
        Espresso.onView(ViewMatchers.withId(R.id.smart_drawing_view))
            .perform(
                DrawingTestUtils.drawStroke(
                    "Drawing straight line",
                    DrawingTestUtils.createStraightLine()
                )
            )

        waitFor(500)

        Espresso.onView(ViewMatchers.withId(R.id.smart_drawing_view))
            .perform(
                DrawingTestUtils.drawStroke(
                    "Drawing zigzag",
                    DrawingTestUtils.createZigzagLine()
                )
            )

        waitFor(500)

        Espresso.onView(ViewMatchers.withId(R.id.smart_drawing_view))
            .perform(
                DrawingTestUtils.drawStroke(
                    "Drawing circle",
                    DrawingTestUtils.createCircle()
                )
            )

        waitFor(2000)
    }

    @Test
    fun testWritingHelloWorld() {
        showTestStep("Writing 'Hello World!'")
        val context = InstrumentationRegistry.getInstrumentation().getTargetContext()

        val allStrokes = HandwritingStrokeHelper.getHelloWorldStrokes(0f, 0f)

        for (stroke in allStrokes) {
            Espresso.onView(ViewMatchers.withId(R.id.smart_drawing_view))
                .perform(DrawingTestUtils.drawStroke("Writing stroke", stroke))
        }

        Espresso.pressBack()
        waitFor(1000) // Final delay to see the result

        // Now verify the data persistence
        val repository = get<HandwrittenNoteRepository>(HandwrittenNoteRepository::class.java)
        val smartNotebookRepository = get<SmartNotebookRepository>(SmartNotebookRepository::class.java)
        val allBooks: MutableList<SmartNotebook> = smartNotebookRepository.allSmartNotebooks

        Assert.assertEquals(1, allBooks.size.toLong())
        Assert.assertEquals(1, allBooks[0].smartBookPages.size.toLong())
        Assert.assertEquals(1, allBooks[0].atomicNotes.size.toLong())

        val atomicNote = allBooks[0].atomicNotes[0]


        // Verify database entry
        val savedNote = repository.getNoteImage(atomicNote, BitmapScale.FULL_SIZE)
        Assert.assertNotNull("HandwrittenNoteWithImage should exist in database", savedNote)
        Assert.assertNotNull("HandwrittenNoteEntity should exist in database", savedNote.handwrittenNoteEntity)
        Assert.assertNotNull("Note image should get loaded", savedNote.noteImage)
        Assert.assertEquals("Note ID should match", atomicNote.noteId, savedNote.handwrittenNoteEntity!!.noteId)

        // Try to load the bitmap to verify it's valid
        val savedBitmap: Bitmap? = savedNote.noteImage
        Assert.assertTrue("Should be able to read saved bitmap", savedBitmap != null)
        Assert.assertNotNull("Bitmap should be valid", savedBitmap)

        // Verify template file exists
        val templateFile = File(savedNote.handwrittenNoteEntity!!.pageTemplateFilePath!!)
        Assert.assertTrue("Template file should exist", templateFile.exists())
        Assert.assertTrue("Template file should not be empty", templateFile.length() > 0)


        val handwrittenNoteEntity: HandwrittenNoteEntity = savedNote.handwrittenNoteEntity!!
        // Verify timestamps
        Assert.assertTrue("Creation time should be set", handwrittenNoteEntity.createdTimeMillis > 0)
        Assert.assertTrue("Last modified time should be set", handwrittenNoteEntity.lastModifiedTimeMillis > 0)
        Assert.assertTrue(
            "Last modified time should be after creation time",
            handwrittenNoteEntity.lastModifiedTimeMillis >= handwrittenNoteEntity.createdTimeMillis
        )
    } // Custom ViewAction to draw strokes
    // Helper methods to create different stroke patterns
}