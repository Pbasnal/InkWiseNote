package com.originb.inkwisenote2.modules.smartnotes.ui

import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteDeleted
import com.originb.inkwisenote2.modules.backgroundjobs.Events.SmartNotebookSaved
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer

class SmartNotebookActivity : AppCompatActivity() {
    private val logger = Logger("SmartNotebookActivity")

    private var workingNotePath: String? = null
    private var indexOfCurrentPage = 0

    private var smartNotebook: SmartNotebook? = null
    private var smartNotebookRepository: SmartNotebookRepository? = null

    private var scrollLayout: SmartNotebookPageScrollLayout? = null
    private var smartNotebookAdapter: SmartNotebookAdapter? = null
    private var recyclerView: RecyclerView? = null

    private var nextButton: FloatingActionButton? = null
    private var prevButton: FloatingActionButton? = null
    private var newNotePageBtn: FloatingActionButton? = null
    private var noteTitleText: EditText? = null
    private var noteCreatedTime: TextView? = null
    private var pageNumText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_note)

        createNoteTitleEditText()
        createNoteCreatedTimeText()

        this.indexOfCurrentPage = 0
        smartNotebookRepository = Repositories.Companion.getInstance().getSmartNotebookRepository()

        recyclerView = findViewById(R.id.smart_note_page_view)
        scrollLayout = SmartNotebookPageScrollLayout(this)
        recyclerView.addOnScrollListener(SmartNotebookScrollListener(scrollLayout!!))
        recyclerView.setLayoutManager(scrollLayout)

        smartNotebookAdapter = SmartNotebookAdapter(this, null)
        BackgroundOps.Companion.executeOpt<SmartNotebook?>(
            Callable<Optional<SmartNotebook?>?> { this.getSmartNotebook() },
            Consumer<SmartNotebook?> { smartNotebook: SmartNotebook? ->
                this.smartNotebook = smartNotebook
                smartNotebookAdapter.setSmartNotebook(smartNotebook)
                noteTitleText.setText(smartNotebook!!.smartBook.title)
                noteCreatedTime!!.text = DateTimeUtils.msToDateTime(smartNotebook.smartBook.createdTimeMillis)
                recyclerView.setAdapter(smartNotebookAdapter)

                if (smartNotebook.getAtomicNotes().size > 1) {
                    nextButton!!.visibility = View.VISIBLE
                }
                updatePageNumberText(smartNotebook)
            })

        createNextNoteButton()
        createPrevNoteButton()
        createNewNoteButton()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNoteDelete(noteDeleted: NoteDeleted) {
        val noteId = noteDeleted.atomicNote.noteId
        smartNotebook!!.removeNote(noteId)

        if (smartNotebook!!.atomicNotes!!.isEmpty()) {
            BackgroundOps.Companion.execute(
                Runnable { smartNotebookRepository!!.deleteSmartNotebook(smartNotebook) },
                Runnable { Routing.HomePageActivity.openHomePageAndStartFresh(this) })
        }

        smartNotebookAdapter!!.removeNoteCard(noteId)
        updatePageNumberText(smartNotebook)
    }

    private fun updatePageNumberText(smartNotebook: SmartNotebook?) {
        pageNumText = findViewById(R.id.page_num_text)
        setPageNum(indexOfCurrentPage)
    }

    private fun createNoteTitleEditText() {
        noteTitleText = this.findViewById(R.id.smart_note_title)
        noteTitleText.setOnClickListener(
            View.OnClickListener { view: View? -> noteTitleText.selectAll() }
        )
        noteTitleText.setOnFocusChangeListener(OnFocusChangeListener { view: View?, hasFocus: Boolean ->
            if (hasFocus) noteTitleText.selectAll()
        })
    }

    private fun createNoteCreatedTimeText() {
        noteCreatedTime = this.findViewById(R.id.note_created_time)
    }

    private fun createNewNoteButton() {
        newNotePageBtn = findViewById(R.id.fab_add_note)
        newNotePageBtn.setOnClickListener(View.OnClickListener { view: View -> this.onNewNotePageClick(view) })
    }

    private fun onNewNotePageClick(view: View) {
        // Determine the position to insert the new item after the currently visible one
        val positionOfNewHolder = indexOfCurrentPage + 1
        BackgroundOps.Companion.execute<SmartNotebook?>(
            Callable<SmartNotebook?> {
                val newAtomicNote = smartNotebookRepository!!.newAtomicNote(
                    "",
                    workingNotePath,
                    NoteType.NOT_SET
                )
                val newSmartPage = smartNotebookRepository!!.newSmartBookPage(
                    smartNotebook!!.smartBook,
                    newAtomicNote, positionOfNewHolder - 1
                )
                smartNotebook!!.insertAtomicNoteAndPage(positionOfNewHolder - 1, newAtomicNote, newSmartPage)

                smartNotebookAdapter!!.saveNotebookPageAt(indexOfCurrentPage, newAtomicNote)
                smartNotebook
            },
            Consumer<SmartNotebook?> { atomicNoteEntity: SmartNotebook? ->
                // Notify the adapter about the new item inserted
                smartNotebookAdapter!!.notifyItemInserted(positionOfNewHolder)
                val totalItemCount = recyclerView!!.adapter!!.itemCount
                if (positionOfNewHolder == totalItemCount) {
                    nextButton!!.visibility = View.INVISIBLE
                } else {
                    nextButton!!.visibility = View.VISIBLE
                }

                prevButton!!.visibility = View.VISIBLE

                scrollLayout.setScrollRequested(true)
                // Optionally scroll to the new item
                recyclerView!!.postDelayed({
                    recyclerView!!.smoothScrollToPosition(positionOfNewHolder)
                    setPageNum(positionOfNewHolder)
                    indexOfCurrentPage = positionOfNewHolder
                }, 10)
            })
    }

    private fun createNextNoteButton() {
        nextButton = findViewById(R.id.fab_next_note)

        nextButton.setVisibility(View.INVISIBLE)
        nextButton.setOnClickListener(View.OnClickListener { view: View? ->
            // Get the total number of items
            val totalItemCount = recyclerView!!.adapter!!.itemCount

            smartNotebookAdapter!!.saveNotebookPageAt(
                indexOfCurrentPage,
                smartNotebook.getAtomicNotes()[indexOfCurrentPage]
            )

            val pageIndex = indexOfCurrentPage + 1

            // hide next button if this is the last visible note
            if (pageIndex >= totalItemCount - 1) {
                nextButton.setVisibility(View.INVISIBLE)
            }
            if (totalItemCount > 1) {
                prevButton!!.visibility = View.VISIBLE
            }

            // Check if we can scroll to the next item
            if (pageIndex < totalItemCount) {
                // Scroll to the next item
                scrollLayout.setScrollRequested(true)
                recyclerView!!.smoothScrollToPosition(pageIndex)
                setPageNum(pageIndex)
                indexOfCurrentPage = pageIndex
            }
        })
    }

    private fun createPrevNoteButton() {
        prevButton = findViewById(R.id.fab_prev_note)
        prevButton.setVisibility(View.INVISIBLE)
        prevButton.setOnClickListener(View.OnClickListener { view: View? ->
            // Get the total number of items
            val totalItemCount = recyclerView!!.adapter!!.itemCount

            smartNotebookAdapter!!.saveNotebookPageAt(
                indexOfCurrentPage,
                smartNotebook.getAtomicNotes()[indexOfCurrentPage]
            )

            val indexOfPrevPage = indexOfCurrentPage - 1

            if (indexOfPrevPage <= 0) {
                prevButton.setVisibility(View.INVISIBLE)
            }
            if (totalItemCount > 1) {
                nextButton!!.visibility = View.VISIBLE
            }
            // Check if we can scroll to the next item
            if (indexOfPrevPage >= 0) {
                // Scroll to the next item
                scrollLayout.setScrollRequested(true)
                recyclerView!!.smoothScrollToPosition(indexOfPrevPage)
                setPageNum(indexOfPrevPage)
                indexOfCurrentPage = indexOfPrevPage
            }
        })
    }

    private fun getSmartNotebook(): Optional<SmartNotebook?>? {
        val bookIdToOpen = intent.getLongExtra("bookId", -1)
        workingNotePath = intent.getStringExtra("workingNotePath")

        if (bookIdToOpen != -1L) {
            return smartNotebookRepository!!.getSmartNotebooks(bookIdToOpen)
        }

        return smartNotebookRepository!!.initializeNewSmartNotebook(
            "",
            workingNotePath,
            NoteType.NOT_SET
        )
    }

    private fun setPageNum(position: Int) {
        pageNumText.setText(
            StringBuilder()
                .append(position + 1)
                .append("/")
                .append(smartNotebook.getAtomicNotes().size)
                .toString()
        )
    }

    override fun onPostResume() {
        super.onPostResume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        smartNotebookAdapter!!.saveNote(noteTitleText!!.text.toString())
        EventBus.getDefault().post(SmartNotebookSaved(smartNotebook, this))
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}

