package com.originb.inkwisenote2.modules.smartnotes.ui

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.DateTimeUtils.msToDateTime
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.common.Routing.HomePageActivity.openSmartHomePageAndStartFresh
import com.originb.inkwisenote2.common.isNotEmpty
import com.originb.inkwisenote2.common.isNullOrWhitespace
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NotebookNavigationData
import com.originb.inkwisenote2.modules.smartnotes.data.SmartNotebookUpdateType
import com.originb.inkwisenote2.modules.smartnotes.ui.activitystates.ISmartNotebookActivityState
import com.originb.inkwisenote2.modules.smartnotes.ui.activitystates.IStateManager
import com.originb.inkwisenote2.modules.smartnotes.viewmodels.SmartNotebookViewModel
import com.originb.inkwisenote2.modules.smartnotes.viewmodels.SmartNotebookViewModel.SmartNotebookUpdate
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import org.koin.android.compat.ViewModelCompat.getViewModel
import org.koin.java.KoinJavaComponent.get
import java.util.*
import java.util.function.Consumer

class SmartNotebookActivity : AppCompatActivity(), IStateManager {
    private val logger = Logger("SmartNotebookActivity")

    private var workingNotePath: String? = null

    private var viewModel: SmartNotebookViewModel? = null
    private var scrollLayout: SmartNotebookPageScrollLayout? = null
    private var smartNotebookAdapter: SmartNotebookAdapter? = null
    private var recyclerView: RecyclerView? = null

    // Dependencies from Koin
    private var handwrittenNoteRepository: HandwrittenNoteRepository? = null
    private var textNotesDao: TextNotesDao? = null
    private var smartNotebookRepository: SmartNotebookRepository? = null
    private var noteOcrTextDao: NoteOcrTextsDao? = null

    private var nextButton: FloatingActionButton? = null
    private var prevButton: FloatingActionButton? = null
    private var newNotePageBtn: FloatingActionButton? = null
    private var noteTitleText: EditText? = null
    private var noteCreatedTime: TextView? = null
    private var pageNumText: TextView? = null
    private var backButton: ImageButton? = null

    private var noteIdToLoadOnOpen: Long? = null
    private var currentState: ISmartNotebookActivityState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_note)

        // Initialize ViewModel with Koin DI
        viewModel = getViewModel<SmartNotebookViewModel>(this, SmartNotebookViewModel::class.java)

        // Get dependencies from Koin
        handwrittenNoteRepository = get<HandwrittenNoteRepository?>(HandwrittenNoteRepository::class.java)
        textNotesDao = get<TextNotesDao?>(TextNotesDao::class.java)
        smartNotebookRepository = get<SmartNotebookRepository?>(SmartNotebookRepository::class.java)
        noteOcrTextDao = get<NoteOcrTextsDao?>(NoteOcrTextsDao::class.java)

        // Load notebook data
        val bookIdToOpen = getIntent().getLongExtra("bookId", -1)
        workingNotePath = getIntent().getStringExtra("workingNotePath")
        val noteIds = getIntent().getStringExtra("noteIds")
        val bookTitle = getIntent().getStringExtra("bookTitle")
        noteIdToLoadOnOpen = getIntent().getLongExtra("selectedNoteId", -1)

        if (noteIds == null || isNullOrWhitespace(noteIds)) {
            currentState = SmartNotebookActivityRWState()
        } else {
            currentState = SmartNotebookActivityVirtualNotebook(this)
        }

        // Initialize UI components
        initializeRecyclerView()
        currentState!!.initializeViews()
        currentState!!.setupObservers()

        viewModel!!.loadSmartNotebook(bookIdToOpen, workingNotePath, bookTitle, noteIds)
    }

    override fun changeState() {
        viewModel!!.onNotebookIsInDb(Consumer { isNotebookSaved: Boolean? ->
            currentState!!.finalizeState()
            currentState = SmartNotebookActivityRWState()
            currentState!!.initializeViews()
        })
    }

    private fun initializeRecyclerView() {
        // Initialize RecyclerView and its components
        recyclerView = findViewById<RecyclerView?>(R.id.smart_note_page_view)
        recyclerView!!.setOverScrollMode(View.OVER_SCROLL_NEVER)
        scrollLayout = SmartNotebookPageScrollLayout(this)
        recyclerView!!.addOnScrollListener(SmartNotebookScrollListener(scrollLayout!!))
        recyclerView!!.setLayoutManager(scrollLayout)

        recyclerView!!.setOnFlingListener(null)
        recyclerView!!.setNestedScrollingEnabled(false)
    }

    fun initializeNavigationButtons() {
        // Initialize buttons
        nextButton = findViewById<FloatingActionButton?>(R.id.fab_next_note)
        prevButton = findViewById<FloatingActionButton?>(R.id.fab_prev_note)

        // Initially hide navigation buttons
        nextButton!!.setVisibility(View.INVISIBLE)
        prevButton!!.setVisibility(View.INVISIBLE)

        // Button click listeners
        nextButton!!.setOnClickListener {
            val atomicNote = viewModel!!.currentNote
            if (atomicNote == null) return@setOnClickListener

            val noteData = smartNotebookAdapter!!.getNoteData(atomicNote.noteId)
            if (noteData == null) return@setOnClickListener
            BackgroundOps.execute(
                Runnable { viewModel!!.saveCurrentNote(viewModel!!.currentNote, noteData) },
                Runnable { viewModel!!.navigateToNextPage() }
            )
        }

        prevButton!!.setOnClickListener {
            val atomicNote = viewModel!!.currentNote
            if (atomicNote == null) return@setOnClickListener

            val noteData = smartNotebookAdapter!!.getNoteData(atomicNote.noteId)
            if (noteData == null) return@setOnClickListener
            BackgroundOps.execute(
                Runnable { viewModel!!.saveCurrentNote(viewModel!!.currentNote, noteData) },
                Runnable { viewModel!!.navigateToPreviousPage() }
            )
        }
    }

    fun initializeNewNoteButton() {
        newNotePageBtn = findViewById<FloatingActionButton>(R.id.fab_add_note)
        newNotePageBtn!!.setImageResource(R.drawable.ic_add)
        newNotePageBtn!!.setVisibility(View.VISIBLE)
        newNotePageBtn!!.setOnClickListener {
            val atomicNote = viewModel!!.currentNote
            val noteData = smartNotebookAdapter!!.getNoteData(atomicNote!!.noteId)
            BackgroundOps.execute(
                Runnable { viewModel!!.saveCurrentNote(viewModel!!.currentNote, noteData) },
                Runnable { viewModel!!.addNewPage() }
            )
        }
    }

    fun initializeNoteTitle() {
        noteTitleText = findViewById<EditText?>(R.id.smart_note_title)
        noteTitleText!!.setOnClickListener { noteTitleText!!.selectAll() }

        noteTitleText!!.setOnFocusChangeListener(OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (hasFocus) noteTitleText!!.selectAll()
        })

        noteTitleText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 0) {
                    noteTitleText!!.setAlpha(1.0f)
                } else {
                    noteTitleText!!.setAlpha(0.7f)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    fun initializeCreatedTimeAndPageNum() {
        // Initialize text fields
        noteCreatedTime = findViewById<TextView?>(R.id.note_created_time)
        pageNumText = findViewById<TextView?>(R.id.page_num_text)
    }

    fun initializeBackButton() {
        backButton = findViewById<ImageButton?>(R.id.back_button)
        if (backButton == null) {
            logger.error("Back button not found in layout. Make sure the layout contains an ImageButton with id 'back_button'")
            return
        }
        backButton!!.setOnClickListener(View.OnClickListener { view: View? ->
            // Save current state before going back
            val atomicNote = viewModel!!.currentNote
            if (atomicNote != null) {
                val noteData = smartNotebookAdapter!!.getNoteData(atomicNote.noteId)
                if (noteData != null) {
                    BackgroundOps.execute(
                        Runnable { viewModel!!.saveCurrentNote(atomicNote, noteData) },
                        Runnable { finish() }
                    )
                } else {
                    finish()
                }
            } else {
                finish()
            }
        })
    }

    /**
     * Hide the navigation bar for immersive drawing experience
     */
    fun hideNavigationBar() {
        val controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
    }

    /**
     * Show the navigation bar
     */
    fun showNavigationBar() {
        val controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
        controller.show(WindowInsetsCompat.Type.navigationBars())
    }

    /**
     * Handle system UI visibility changes to maintain immersive mode
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Check if current fragment is HandwrittenNoteFragment and hide navigation bar if needed
            if (smartNotebookAdapter != null) {
                val currentNote = viewModel!!.currentNote
                if (currentNote != null && "handwritten_png" == currentNote.noteType) {
                    hideNavigationBar()
                }
            }
        }
    }

    fun onSmartNotebookUpdate(notebookUpdate: SmartNotebookUpdate) {
        if (isFinishing || isDestroyed) {
            return  // Don't proceed if activity is finishing/destroyed
        }

        if (notebookUpdate.notbookUpdateType == SmartNotebookUpdateType.NOTEBOOK_DELETED) {
            currentState = SmartNotebookDeletedNotebook()
            openSmartHomePageAndStartFresh(this)
            return
        }

        if (smartNotebookAdapter == null) {
            smartNotebookAdapter = SmartNotebookAdapter(
                this, notebookUpdate.smartNotebook,
                smartNotebookRepository!!, handwrittenNoteRepository, textNotesDao, noteOcrTextDao
            )
            if (recyclerView != null) {
                recyclerView!!.setAdapter(smartNotebookAdapter)
            }
        }

        if (notebookUpdate.notbookUpdateType == SmartNotebookUpdateType.NOTE_DELETED) {
            if (smartNotebookAdapter != null) {
                smartNotebookAdapter!!.removeNoteCard(notebookUpdate.atomicNote?.noteId!!)
            }
        } else if (notebookUpdate.indexOfUpdatedNote == -1) {
            if (smartNotebookAdapter != null) {
                smartNotebookAdapter!!.setSmartNotebook(notebookUpdate.smartNotebook)
            }
        } else {
            if (smartNotebookAdapter != null) {
                smartNotebookAdapter!!.setSmartNotebook(notebookUpdate.smartNotebook, notebookUpdate.indexOfUpdatedNote)
            }
        }

        // Update UI elements if available
        val createdTime = msToDateTime(notebookUpdate.smartNotebook.smartBook.lastModifiedTimeMillis)
        if (noteCreatedTime != null) {
            noteCreatedTime!!.setText(createdTime)
        }

        if (noteTitleText != null) {
            val noteTitle = noteTitleText!!.getText().toString().trim { it <= ' ' }
            if (isNullOrWhitespace(noteTitle)) {
                val smartBookName = notebookUpdate.smartNotebook.smartBook.title
                if (isNotEmpty(smartBookName)) {
                    noteTitleText!!.setText(notebookUpdate.smartNotebook.smartBook.title)
                    noteTitleText!!.setAlpha(1.0f)
                }
            }
        }

        if (noteIdToLoadOnOpen != null && noteIdToLoadOnOpen != -1L) {
            val allNotes = notebookUpdate.smartNotebook.atomicNotes
            var i = 0
            while (i < allNotes.size) {
                if (noteIdToLoadOnOpen == allNotes[i].noteId) {
                    break
                }
                i++
            }
            viewModel!!.navigateToPageIndex(i)
            noteIdToLoadOnOpen = -1L
        }
    }

    fun onNavigationDataChange(navigationData: NotebookNavigationData?) {
        if (isFinishing() || isDestroyed() || navigationData == null) {
            return
        }

        if (pageNumText != null) {
            pageNumText!!.setText(navigationData.pageNumbeText)
        }

        if (nextButton != null) {
            nextButton!!.setVisibility(if (navigationData.showNextButton) View.VISIBLE else View.INVISIBLE)
        }

        if (prevButton != null) {
            prevButton!!.setVisibility(if (navigationData.showPrevButton) View.VISIBLE else View.INVISIBLE)
        }
    }

    fun onCurrentPageIndexChange(index: Int) {
        if (recyclerView == null || scrollLayout == null || smartNotebookAdapter == null) {
            return  // Guard against null references
        }

        recyclerView!!.postDelayed({
            if (!isDestroyed && !isFinishing) {
                scrollLayout!!.setScrollRequested(true)
                recyclerView!!.smoothScrollToPosition(index)

                // Get current note and ensure it's not null before trying to set data
                val currentNote = viewModel!!.currentNote
                if (currentNote != null) {
                    smartNotebookAdapter!!.setNoteData(index, currentNote)
                }
            }
        }, 100)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        currentState!!.saveNotebook()
    }

    override fun onStop() {
        super.onStop()
        currentState!!.saveNotebook()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Recreate all visual elements to apply new theme
        recreateVisuals()

        // After view recreation is complete, recreate all fragments with new theme
        if (recyclerView != null) {
            recyclerView!!.post(Runnable { this.recreateFragments() })
        }
    }

    private fun recreateVisuals() {
        // Get current content view to be replaced
        val rootView = findViewById<ViewGroup?>(android.R.id.content) ?: return

        // Save state of important elements
        var currentNote: AtomicNoteEntity? = null
        if (viewModel != null) {
            currentNote = viewModel!!.currentNote
        }
        var currentIndex: Int? = null
        if (viewModel != null) {
            currentIndex = viewModel!!.getCurrentPageIndexLive().getValue()
        }
        var currentNotebook: SmartNotebook? = null
        if (viewModel != null && viewModel!!.getSmartNotebookUpdate().getValue() != null) {
            currentNotebook = viewModel!!.getSmartNotebookUpdate().getValue()!!.smartNotebook
        }

        // Re-apply theme by recreating the views
        setContentView(R.layout.activity_smart_note)

        // Reinitialize the UI elements
        initializeRecyclerView()

        // Create new adapter with the notebook data
        if (currentNotebook != null) {
            smartNotebookAdapter = SmartNotebookAdapter(
                this, currentNotebook,
                smartNotebookRepository!!, handwrittenNoteRepository, textNotesDao, noteOcrTextDao
            )
            recyclerView!!.setAdapter(smartNotebookAdapter)
        }

        // Re-initialize the current state
        if (currentState != null) {
            currentState!!.initializeViews()
            currentState!!.setupObservers()
        }

        // Update the adapter with the current notebook data
        if (viewModel != null && viewModel!!.getSmartNotebookUpdate().getValue() != null) {
            val update = viewModel!!.getSmartNotebookUpdate().getValue()
            onSmartNotebookUpdate(update!!)

            // If we had a note and index already, restore the position
            if (currentIndex != null) {
                onCurrentPageIndexChange(currentIndex)
            }
        }
    }

    private fun recreateFragments() {
        if (smartNotebookAdapter == null || viewModel == null) return

        // Force recreate all fragments to apply new theme
        val update = viewModel!!.getSmartNotebookUpdate().getValue()
        if (update != null) {
            // Set notebook to null and back to force a full refresh of all fragments
            smartNotebookAdapter!!.refreshFragments()
            smartNotebookAdapter!!.setSmartNotebook(update.smartNotebook)

            // Ensure we're showing the current page
            val currentPageIndex = viewModel!!.getCurrentPageIndexLive().getValue()
            if (currentPageIndex != null) {
                onCurrentPageIndexChange(currentPageIndex)
            }
        }
    }

    inner class SmartNotebookActivityRWState : ISmartNotebookActivityState {
        override fun initializeViews() {
            initializeNavigationButtons()
            initializeNewNoteButton()
            initializeNoteTitle()
            initializeCreatedTimeAndPageNum()
            initializeBackButton()
        }

        override fun finalizeState() {
            // Remove observers to prevent duplicates
            if (viewModel != null) {
                viewModel!!.getSmartNotebookUpdate().removeObservers(this@SmartNotebookActivity)
                viewModel!!.getNavigationDataLive().removeObservers(this@SmartNotebookActivity)
                viewModel!!.getCurrentPageIndexLive().removeObservers(this@SmartNotebookActivity)
            }
        }

        override fun setupObservers() {
            val owner = this@SmartNotebookActivity

            // First remove any existing observers to prevent duplication during configuration changes
            if (viewModel != null) {
                viewModel!!.getSmartNotebookUpdate().removeObservers(owner)
                viewModel!!.getNavigationDataLive().removeObservers(owner)
                viewModel!!.getCurrentPageIndexLive().removeObservers(owner)
            }

            // Observe smart notebook data changes
            viewModel!!.getSmartNotebookUpdate().observe(owner, Observer { notebookUpdate: SmartNotebookUpdate? ->
                if (notebookUpdate != null) {
                    onSmartNotebookUpdate(notebookUpdate)
                }
            })

            // Observe page number text
            viewModel!!.getNavigationDataLive().observe(owner, Observer { navigationData: NotebookNavigationData? ->
                if (navigationData != null) {
                    onNavigationDataChange(navigationData)
                }
            })

            // Observe current page index (for scrolling)
            viewModel!!.getCurrentPageIndexLive().observe(owner, Observer { index: Int? ->
                if (index != null) {
                    onCurrentPageIndexChange(index)
                }
            })
        }

        override fun saveNotebook() {
            val oldNotebookTitle = viewModel!!.getNotebookTitle().getValue()
            val updatedTitle = noteTitleText!!.getText().toString().trim { it <= ' ' }
            val createTimeMillis = viewModel!!.getCreatedTimeMillis().getValue().toString()
            val notebookTitle = if (isNotEmpty(updatedTitle)) updatedTitle else createTimeMillis
            val notebookNameNotChanged = oldNotebookTitle != null && oldNotebookTitle == notebookTitle

            val titleUpdated = !notebookNameNotChanged && viewModel!!.updateTitle(updatedTitle)

            if (!titleUpdated) {
                val noteHolderData = smartNotebookAdapter!!.getNoteData(viewModel!!.currentNote?.noteId ?: -1L)
                BackgroundOps.execute(Runnable {
                    viewModel!!.saveCurrentNote(viewModel!!.currentNote, noteHolderData)
                    viewModel!!.saveCurrentSmartNotebook()
                })
            } else {
                viewModel!!.saveCurrentSmartNotebook()
                val newNotebookPath = workingNotePath + "/" + notebookTitle
                val isRenamed = viewModel!!.renameNotebookFolderName(newNotebookPath, oldNotebookTitle)
                val notebook = Objects.requireNonNull<SmartNotebookUpdate?>(
                    viewModel!!.getSmartNotebookUpdate().getValue()
                ).smartNotebook
                var notesHaveNewPath = true

                for (atomicNote in notebook.atomicNotes) {
                    val noteFilePath = atomicNote!!.filepath
                    notesHaveNewPath = notesHaveNewPath and (newNotebookPath == noteFilePath)
                }

                if (isRenamed || !notesHaveNewPath) {
                    logger.debug("Folder renamed successfully.")
                    for (atomicNote in notebook.atomicNotes) {
                        val noteHolderData = smartNotebookAdapter!!.getNoteData(atomicNote.noteId)
                        viewModel!!.saveNoteInCorrectFolder(atomicNote, newNotebookPath, noteHolderData)
                    }
                } else {
                    logger.debug("Failed to rename folder.")
                }
            }
        }
    }

    inner class SmartNotebookDeletedNotebook : ISmartNotebookActivityState {
        override fun initializeViews() {
        }

        override fun finalizeState() {
        }

        override fun setupObservers() {
        }

        override fun saveNotebook() {
        }
    }

    inner class SmartNotebookActivityVirtualNotebook(private val stateManager: IStateManager) :
        ISmartNotebookActivityState {
        override fun initializeViews() {
            initializeNavigationButtons()
            initializeSaveButton_VirtualNotebook()
            initializeNoteTitle_VirtualNotebook()
            initializeCreatedTimeAndPageNum()
            initializeBackButton()
        }

        override fun finalizeState() {
            // Remove observers to prevent duplicates
            if (viewModel != null) {
                viewModel!!.getSmartNotebookUpdate().removeObservers(this@SmartNotebookActivity)
                viewModel!!.getNavigationDataLive().removeObservers(this@SmartNotebookActivity)
                viewModel!!.getCurrentPageIndexLive().removeObservers(this@SmartNotebookActivity)
            }
        }

        override fun setupObservers() {
            val owner = this@SmartNotebookActivity

            // First remove any existing observers to prevent duplication during configuration changes
            if (viewModel != null) {
                viewModel!!.getSmartNotebookUpdate().removeObservers(owner)
                viewModel!!.getNavigationDataLive().removeObservers(owner)
                viewModel!!.getCurrentPageIndexLive().removeObservers(owner)
            }

            // Observe smart notebook data changes
            viewModel!!.getSmartNotebookUpdate().observe(owner, Observer { smartNotebookUpdate: SmartNotebookUpdate? ->
                if (smartNotebookUpdate != null) {
                    onSmartNotebookUpdate_VirtualNotebook(smartNotebookUpdate)
                    onSmartNotebookUpdate(smartNotebookUpdate)
                }
            })

            // Observe page number text
            viewModel!!.getNavigationDataLive().observe(owner, Observer { navigationData: NotebookNavigationData? ->
                if (navigationData != null) {
                    onNavigationDataChange(navigationData)
                }
            })

            // Observe current page index (for scrolling)
            viewModel!!.getCurrentPageIndexLive().observe(owner, Observer { index: Int? ->
                if (index != null) {
                    onCurrentPageIndexChange(index)
                }
            })
        }

        private fun onSmartNotebookUpdate_VirtualNotebook(smartNotebookUpdate: SmartNotebookUpdate) {
            val notebook = smartNotebookUpdate.smartNotebook
            val bookId: Long = notebook.smartBook.bookId
            if (bookId != -1L) {
                stateManager.changeState()
            }
        }

        override fun saveNotebook() {
            val atomicNote = viewModel!!.currentNote ?: return
            val noteHolderData = smartNotebookAdapter!!.getNoteData(atomicNote.noteId)
            BackgroundOps.execute(Runnable { viewModel!!.saveCurrentNote(atomicNote, noteHolderData) })
            // SmartNotebook is not saved
        }

        private fun initializeSaveButton_VirtualNotebook() {
            newNotePageBtn = findViewById<FloatingActionButton>(R.id.fab_add_note)
            newNotePageBtn!!.setVisibility(View.GONE)
        }

        private fun initializeNoteTitle_VirtualNotebook() {
            noteTitleText = findViewById<EditText?>(R.id.smart_note_title)
            noteTitleText!!.setFocusable(false)
            noteTitleText!!.setCursorVisible(false)
            noteTitleText!!.setInputType(InputType.TYPE_NULL)
        }
    }
}

