package com.originb.inkwisenote2.modules.smartnotes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.config.ConfigReader
import com.originb.inkwisenote2.config.ConfigReader.Companion.getInstance
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import com.originb.inkwisenote2.modules.handwrittennotes.PageBackgroundType
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate
import com.originb.inkwisenote2.modules.handwrittennotes.ui.DrawingView
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao

class HandwrittenNoteFragment(
    smartNotebook: SmartNotebook?,
    atomicNote: AtomicNoteEntity?,
    private val handwrittenNoteRepository: HandwrittenNoteRepository,
// Additional dependencies for NoteDebugDialog
    private val textNotesDao: TextNotesDao?,
    private val noteOcrTextDao: NoteOcrTextsDao?,
    private val smartNotebookRepository: SmartNotebookRepository?
) : NoteFragment(smartNotebook, atomicNote) {
    private val logger = Logger("HandwrittenNoteFragment")

    private var fragmentView: View? = null
    private var drawingView: DrawingView? = null
    private var deleteNote: ImageButton? = null
    private var debugButton: ImageButton? = null
    private var eraserButton: ImageButton? = null
    private var pencilButton: ImageButton? = null

    private val configReader: ConfigReader


    init {
        configReader = getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.note_drawing_fragment, container, false)
        drawingView = fragmentView!!.findViewById<DrawingView?>(R.id.smart_drawing_view)
        deleteNote = fragmentView!!.findViewById<ImageButton>(R.id.delete_note)
        debugButton = fragmentView!!.findViewById<ImageButton>(R.id.debug_button)
        eraserButton = fragmentView!!.findViewById<ImageButton>(R.id.eraser_button)
        pencilButton = fragmentView!!.findViewById<ImageButton>(R.id.pencil_button)

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Delete note button listener
        deleteNote!!.setOnClickListener(View.OnClickListener { v: View? ->
            confirmDeleteNote()
        })

        // Debug button listener
        debugButton!!.setOnClickListener(View.OnClickListener { v: View? ->
            showDebugDialog()
        })

        // Set up eraser button listener
        eraserButton!!.setOnClickListener(View.OnClickListener { v: View? ->
            activateEraserMode()
        })

        // Set up pencil button listener
        pencilButton!!.setOnClickListener(View.OnClickListener { v: View? ->
            activatePencilMode()
        })

        // Start in pencil mode by default
        activatePencilMode()

        loadNote()
    }

    override fun onResume() {
        super.onResume()
        // Hide navigation bar when fragment becomes active
        if (getActivity() is SmartNotebookActivity) {
            (getActivity() as SmartNotebookActivity).hideNavigationBar()
        }
    }

    override fun onPause() {
        super.onPause()
        // Show navigation bar when fragment becomes inactive
        if (getActivity() is SmartNotebookActivity) {
            (getActivity() as SmartNotebookActivity).showNavigationBar()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Ensure navigation bar is shown when fragment is destroyed
        if (getActivity() is SmartNotebookActivity) {
            (getActivity() as SmartNotebookActivity).showNavigationBar()
        }
    }

    /**
     * Ensure navigation bar stays hidden during drawing
     */
    fun ensureNavigationBarHidden() {
        if (getActivity() is SmartNotebookActivity) {
            (getActivity() as SmartNotebookActivity).hideNavigationBar()
        }
    }

    /**
     * Switch to eraser mode
     */
    private fun activateEraserMode() {
        if (drawingView != null) {
            drawingView!!.setEraserMode(true)

            // Highlight eraser button, unhighlight pencil button
            eraserButton!!.setAlpha(1.0f)
            pencilButton!!.setAlpha(0.5f)
        }
    }

    /**
     * Switch to pencil (drawing) mode
     */
    private fun activatePencilMode() {
        if (drawingView != null) {
            drawingView!!.setEraserMode(false)

            // Highlight pencil button, unhighlight eraser button
            pencilButton!!.setAlpha(1.0f)
            eraserButton!!.setAlpha(0.5f)
        }
    }

    protected fun loadNote() {
        if (atomicNote == null) return

        // Load strokes from markdown file
        execute(
            Runnable { handwrittenNoteRepository.readHandwrittenNoteMarkdown(atomicNote) },
            Runnable { strokes ->
                if (drawingView != null && strokes != null && !strokes.isEmpty()) {
                    drawingView!!.setStrokes(strokes)
                }
            }
        )

        // Load the page template
        execute(
            Runnable { handwrittenNoteRepository.getPageTemplate(atomicNote) },
            Runnable { pageTemplateOpt ->
                if (pageTemplateOpt.isPresent() && drawingView != null) {
                    drawingView!!.pageTemplate = pageTemplateOpt.get()
                    return@execute
                }
                // Create a new page template if none exists
                val pageTemplate: PageTemplate? = configReader.getAppConfig().getPageTemplates()
                    .get(PageBackgroundType.BASIC_RULED_PAGE_TEMPLATE.name)

                if (drawingView != null) {
                    drawingView!!.pageTemplate = pageTemplate
                }
                execute(Runnable { handwrittenNoteRepository.saveHandwrittenNotePageTemplate(atomicNote, pageTemplate) }
                )
            }
        )
    }

    private fun showDebugDialog() {
        if (getContext() != null) {
            val dialog = NoteDebugDialog(
                getContext()!!, atomicNote, smartNotebook,
                smartNotebookRepository, textNotesDao, noteOcrTextDao, handwrittenNoteRepository
            )
            dialog.show()
        }
    }

    override fun getNoteHolderData(): NoteHolderData {
        if (drawingView == null) {
            return NoteHolderData.Companion.handWrittenNoteData(null, null)
        }

        return NoteHolderData.Companion.handWrittenNoteData(
            drawingView!!.bitmap,
            drawingView!!.pageTemplate,
            drawingView!!.strokes
        )
    }
}
