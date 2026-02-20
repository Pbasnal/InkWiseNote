package com.originb.inkwisenote2.modules.admin

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.admin.AdminUiState.DataList
import com.originb.inkwisenote2.modules.admin.AdminUiState.FilesState
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteEntity
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPage
import org.koin.android.compat.ViewModelCompat.getViewModel

class AdminActivity : AppCompatActivity() {
    private var tableLayout: TableLayout? = null
    private var editText: EditText? = null
    private var filterQueryBtn: Button? = null
    private var selectedTab = "Term Frequencies"
    private var viewModel: AdminViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // UI Initialization
        editText = findViewById<EditText>(R.id.note_id_query)
        filterQueryBtn = findViewById<Button>(R.id.filter_query)
        tableLayout = findViewById<TableLayout>(R.id.data_table)
        val tabLayout = findViewById<TabLayout>(R.id.table_selector_tabs)

        // ViewModel Initialization
        viewModel = getViewModel<AdminViewModel>(this, AdminViewModel::class.java)

        // Observers
        observeViewModel()

        // Listeners
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedTab = tab.getText().toString()
                triggerRefresh()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        filterQueryBtn!!.setOnClickListener(View.OnClickListener { v: View? -> triggerRefresh() })

        //         Initial Load
        triggerRefresh()
    }

    private fun observeViewModel() {
        viewModel!!.uiState.observe(this, Observer { state: AdminUiState? ->
            if (state is DataList) {
                renderDbTable(state)
            } else if (state is FilesState) {
                renderFilesTable(state)
            }
        })

        viewModel!!.toastMessage.observe(this, Observer { msg: String? ->
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        })
    }

    private fun triggerRefresh() {
        val input = editText!!.getText().toString()
        val noteId = if (input.isEmpty()) 0L else input.toLong()
        viewModel!!.loadData(selectedTab, noteId, getFilesDir())
    }

    // --- Rendering Logic ---
    private fun renderDbTable(state: DataList) {
        tableLayout!!.removeAllViews()

        if (state.data == null || state.data.isEmpty()) {
            renderEmptyState("No data available for this category")
            return
        }

        val headerRow = TableRow(this)

        // Column Header Logic
        when (state.type) {
            "Term Frequencies" -> addHeaderCell(headerRow, "Note ID", "Term", "Frequency")
            "Note Text" -> addHeaderCell(headerRow, "Note ID", "Text")
            "Atomic Notes" -> addHeaderCell(headerRow, "Note ID", "Filename", "Filepath", "Type")
            "Smart Books" -> addHeaderCell(headerRow, "Book ID", "Title")
            "Smart Book Pages" -> addHeaderCell(headerRow, "Book ID", "Note ID")
            "Handwritten Notes" -> addHeaderCell(headerRow, "Note ID", "Book ID", "Bitmap", "PageT")
        }
        tableLayout!!.addView(headerRow)

        // Data Row Logic
        for (item in state.data) {
            val row = TableRow(this)
            if (item is NoteTermFrequency) {
                val e = item
                addCells(row, e.noteId.toString(), e.term, e.termFrequency.toString())
            } else if (item is NoteOcrText) {
                val e = item
                addCells(row, e.noteId.toString(), e.extractedText)
            } else if (item is AtomicNoteEntity) {
                val e = item
                addCells(row, e.noteId.toString(), e.filename, e.filepath, e.noteType)
            } else if (item is SmartBookEntity) {
                val e = item
                addCells(row, e.bookId.toString(), e.title)
            } else if (item is SmartBookPage) {
                val e = item
                addCells(row, e.bookId.toString(), e.noteId.toString())
            } else if (item is HandwrittenNoteEntity) {
                val e = item
                addCells(
                    row,
                    e.noteId.toString(),
                    e.bookId.toString(),
                    e.bitmapFilePath,
                    e.pageTemplateFilePath
                )
            }
            tableLayout!!.addView(row)
        }
    }

    private fun renderEmptyState(message: String?) {
        val row = TableRow(this)
        val tv = TextView(this)
        tv.setText(message)
        tv.setPadding(20, 20, 20, 20)
        row.addView(tv)
        tableLayout!!.addView(row)
    }

    private fun renderFilesTable(state: FilesState) {
        tableLayout!!.removeAllViews()

        // Navigation Row
        val navRow = TableRow(this)
        val pathView = TextView(this)
        pathView.setText(getString(R.string.path_label) + state.currentDir.getAbsolutePath())
        pathView.setPadding(16, 16, 16, 16)
        navRow.addView(pathView)

        val upBtn = Button(this)
        upBtn.setText(getString(R.string.up_button))
        upBtn.setOnClickListener(View.OnClickListener { v: View? -> viewModel!!.navigateToDir(state.currentDir.getParentFile()) })
        navRow.addView(upBtn)
        tableLayout!!.addView(navRow)

        // Header
        val headerRow = TableRow(this)
        addHeaderCell(headerRow, "Name", "Size", "Type", "Action")
        tableLayout!!.addView(headerRow)

        // File Rows
        if (state.files != null) {
            for (file in state.files) {
                val row = TableRow(this)
                addCells(row, file.getName(), file.length().toString(), if (file.isDirectory()) "DIR" else "FILE")

                val actions = LinearLayout(this)
                if (file.isDirectory()) {
                    val open = Button(this)
                    open.setText(getString(R.string.open_button))
                    open.setOnClickListener(View.OnClickListener { v: View? -> viewModel!!.navigateToDir(file) })
                    actions.addView(open)
                }
                val del = Button(this)
                del.setText(getString(R.string.delete_button))
                del.setOnClickListener(View.OnClickListener { v: View? -> viewModel!!.deleteFile(file) })
                actions.addView(del)

                row.addView(actions)
                tableLayout!!.addView(row)
            }
        }
    }

    // --- Helpers ---
    private fun addHeaderCell(row: TableRow, vararg titles: String?) {
        for (title in titles) {
            val tv = TextView(this)
            tv.setText(title)
            tv.setPadding(16, 16, 16, 16)
            tv.setTypeface(null, Typeface.BOLD)
            row.addView(tv)
        }
    }

    private fun addCells(row: TableRow, vararg contents: String?) {
        for (content in contents) {
            val tv = TextView(this)
            tv.setText(content)
            tv.setPadding(16, 16, 16, 16)
            row.addView(tv)
        }
    }
}