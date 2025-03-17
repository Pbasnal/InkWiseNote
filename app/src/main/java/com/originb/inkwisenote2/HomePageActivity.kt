package com.originb.inkwisenote2

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.config.*
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao
import com.originb.inkwisenote2.modules.notesearch.NoteSearchActivity
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.sidebar.HomePageSidebarUiComponent
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNoteGridAdapter
import java.util.concurrent.Callable
import java.util.function.Consumer

class HomePageActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var smartNoteGridAdapter: SmartNoteGridAdapter? = null

    private var noteSearchButton: ImageButton? = null

    private var configReader: ConfigReader? = null

    private var fabMenuContainer: LinearLayout? = null
    private var mainFab: FloatingActionButton? = null
    private var isFabMenuOpen = false

    private var smartNotebookRepository: SmartNotebookRepository? = null
    private var noteRelationDao: NoteRelationDao? = null

    private var homePageSidebarUiComponent: HomePageSidebarUiComponent? = null

    private var repositories: Repositories? = null

    private val logger = Logger("HomePageActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        configReader = ConfigReader.Companion.fromContext(this)
        repositories = Repositories.Companion.registerRepositories(this)

        smartNotebookRepository = repositories.getSmartNotebookRepository()
        noteRelationDao = repositories.getNotesDb().noteRelationDao()

        createGridLayoutToShowNotes()
        if (configReader!!.isFeatureEnabled(Feature.MARKDOWN_EDITOR) || configReader!!.isFeatureEnabled(Feature.CAMERA_NOTE)) {
            setupFabMenu()
        } else {
            createNewNoteButton()
        }

        createSearchBtn()
        createSidebarIfEnabled()
        observeAppState()
    }

    private fun observeAppState() {
        val ocrAzureStatusIndicator = findViewById<ImageButton>(R.id.ocr_status)
        AppState.Companion.observeIfAzureOcrRunning(this, Observer<Boolean> { isAzureOcrRunning: Boolean ->
            if (isAzureOcrRunning) {
                ocrAzureStatusIndicator.colorFilter = null
            } else {
                ocrAzureStatusIndicator.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
            }
        })
        AppState.Companion.updateState()
    }

    private fun createSearchBtn() {
        noteSearchButton = findViewById(R.id.btn_search_note)
        noteSearchButton.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(this@HomePageActivity, NoteSearchActivity::class.java)
            startActivity(intent)
        })
    }

    private fun createSidebarIfEnabled() {
        if (!configReader!!.isFeatureEnabled(Feature.HOME_PAGE_NAVIGATION_SIDEBAR)) return
        homePageSidebarUiComponent = HomePageSidebarUiComponent(this, configReader)
        homePageSidebarUiComponent!!.createSidebarIfEnabled()
    }

    fun createGridLayoutToShowNotes() {
        recyclerView = findViewById(R.id.note_card_grid_view)
        val gridLayoutManager = GridLayoutManager(this, 2)
        recyclerView.setLayoutManager(gridLayoutManager)

        smartNoteGridAdapter = SmartNoteGridAdapter(this, ArrayList())

        BackgroundOps.Companion.execute<List<NoteRelation>?>(
            Callable<List<NoteRelation?>?> { noteRelationDao.getAllNoteRelations() },
            Consumer<List<NoteRelation?>?> { relatedNotes: List<NoteRelation?>? ->
                AppState.Companion.updatedRelatedNotes(
                    relatedNotes
                )
            }
        )

        recyclerView.setAdapter(smartNoteGridAdapter)
        recyclerView.setHasFixedSize(true)
    }

    fun createNewNoteButton() {
        val fab = findViewById<FloatingActionButton>(R.id.fab_add_note)
        fab.setOnClickListener { v: View? ->
            Routing.SmartNotebookActivity.newNoteIntent(
                this, filesDir.path
            )
        }
    }

    override fun onResume() {
        super.onResume()
        BackgroundOps.Companion.execute<List<SmartNotebook>?>(
            Callable<List<SmartNotebook?>?> { smartNotebookRepository.getAllSmartNotebooks() },
            Consumer<List<SmartNotebook>?> { smartNotebooks: List<SmartNotebook>? ->
                logger.debug("Setting smartNotebooks", smartNotebooks)
                smartNoteGridAdapter!!.setSmartNotebooks(smartNotebooks)
            }
        )
    }

    override fun onBackPressed() {
        if (isFabMenuOpen) {
            closeFabMenu()
        }
        homePageSidebarUiComponent!!.onBackPressed()
        super.onBackPressed()
    }

    private fun setupFabMenu() {
        fabMenuContainer = findViewById(R.id.fab_menu_container)
        mainFab = findViewById(R.id.new_note_opt)
        val newNoteFab = findViewById<FloatingActionButton>(R.id.fab_add_note)
        newNoteFab.visibility = View.GONE

        mainFab.setVisibility(View.VISIBLE)

        // Setup main FAB click
        mainFab.setOnClickListener(View.OnClickListener { v: View? -> toggleFabMenu() })

        // Setup individual FABs
        val fabCamera = findViewById<FloatingActionButton>(R.id.fab_camera)
        val fabHandwritten = findViewById<FloatingActionButton>(R.id.fab_handwritten)
        val fabText = findViewById<FloatingActionButton>(R.id.fab_text)

        fabHandwritten.setOnClickListener { v: View? ->
            toggleFabMenu()
            // Handle handwritten note creation
            Routing.SmartNotebookActivity.newNoteIntent(this, filesDir.path)
        }

        if (configReader!!.isFeatureEnabled(Feature.CAMERA_NOTE)) {
            findViewById<View>(R.id.fab_camera_menu_item).visibility = View.VISIBLE
            fabCamera.setOnClickListener { v: View? ->
                toggleFabMenu()
                // Handle camera note creation
                Routing.SmartNotebookActivity.newNoteIntent(this, filesDir.path)
            }
        } else {
            findViewById<View>(R.id.fab_camera_menu_item).visibility = View.GONE
        }


        if (configReader!!.isFeatureEnabled(Feature.MARKDOWN_EDITOR)) {
            findViewById<View>(R.id.fab_text_menu_item).visibility = View.VISIBLE
            fabText.setOnClickListener { v: View? ->
                toggleFabMenu()
                // Handle text note creation
                Routing.TextNoteActivity.newNoteIntent(this, filesDir.path)
            }
        } else {
            findViewById<View>(R.id.fab_text_menu_item).visibility = View.INVISIBLE
        }
    }

    private fun toggleFabMenu() {
        if (!isFabMenuOpen) {
            showFabMenu()
        } else {
            closeFabMenu()
        }
    }

    private fun showFabMenu() {
        isFabMenuOpen = true
        fabMenuContainer!!.visibility = View.VISIBLE
        mainFab!!.animate().rotation(45f)
        fabMenuContainer!!.animate()
            .alpha(1f)
            .translationY(0f)
            .setInterpolator(OvershootInterpolator(1.0f))
            .start()
    }

    private fun closeFabMenu() {
        isFabMenuOpen = false
        mainFab!!.animate().rotation(0f)
        fabMenuContainer!!.animate()
            .alpha(0f)
            .translationY(fabMenuContainer!!.height.toFloat())
            .setInterpolator(AnticipateInterpolator(1.0f))
            .withEndAction { fabMenuContainer!!.visibility = View.GONE }
            .start()
    }
}
