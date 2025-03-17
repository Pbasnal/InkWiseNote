package com.originb.inkwisenote2.modules.admin

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.util.CollectionUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteEntity
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNotesDao
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.smartnotes.data.*
import java.util.concurrent.Callable
import java.util.function.Consumer

class AdminActivity : AppCompatActivity() {
    private var tableLayout: TableLayout? = null
    private var noteTermFrequencyDao: NoteTermFrequencyDao? = null
    private var noteOcrTextDao: NoteOcrTextDao? = null
    private var atomicNoteEntitiesDao: AtomicNoteEntitiesDao? = null
    private var smartBooksDao: SmartBooksDao? = null
    private var smartBookPagesDao: SmartBookPagesDao? = null
    private var handwrittenNotesDao: HandwrittenNotesDao? = null

    private var editText: EditText? = null
    private var filterQueryBtn: Button? = null
    private var selectedTab = "Term Frequencies"

    private val tablePopulators: MutableMap<String, Consumer<Long>> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        editText = findViewById(R.id.note_id_query)
        filterQueryBtn = findViewById(R.id.filter_query)
        tableLayout = findViewById(R.id.data_table)

        noteTermFrequencyDao = Repositories.Companion.getInstance().getNotesDb().noteTermFrequencyDao()
        noteOcrTextDao = Repositories.Companion.getInstance().getNotesDb().noteOcrTextDao()
        atomicNoteEntitiesDao = Repositories.Companion.getInstance().getNotesDb().atomicNoteEntitiesDao()
        smartBooksDao = Repositories.Companion.getInstance().getNotesDb().smartBooksDao()
        smartBookPagesDao = Repositories.Companion.getInstance().getNotesDb().smartBookPagesDao()
        handwrittenNotesDao = Repositories.Companion.getInstance().getNotesDb().handwrittenNotesDao()

        tablePopulators["Term Frequencies"] = Consumer { noteId: Long -> this.showTermFrequencyData(noteId) }
        tablePopulators["Note Text"] = Consumer { noteId: Long -> this.showNoteTextData(noteId) }
        tablePopulators["Atomic Notes"] = Consumer { noteId: Long -> this.showNoteAtomicNotes(noteId) }
        tablePopulators["Smart Books"] = Consumer { noteId: Long -> this.showSmartBooks(noteId) }
        tablePopulators["Smart Book Pages"] = Consumer { noteId: Long -> this.showSmartBookPages(noteId) }
        tablePopulators["Handwritten Notes"] = Consumer { noteId: Long -> this.showHandWrittenNotes(noteId) }

        val tabLayout = findViewById<TabLayout>(R.id.table_selector_tabs)
        tabLayout.addOnTabSelectedListener(object : TableSelector() {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val noteId = editText.getText().toString().toLong()
                selectedTab = tab.text.toString()
                tablePopulators[selectedTab]!!.accept(noteId)
            }
        })

        // Show term frequency data by default
        showTermFrequencyData(0L)

        filterQueryBtn.setOnClickListener(View.OnClickListener { btn: View? ->
            val noteId = editText.getText().toString().toLong()
            tablePopulators[selectedTab]!!.accept(noteId)
        })
    }

    private fun showHandWrittenNotes(noteId: Long) {
        tableLayout!!.removeAllViews()

        // Add header
        val headerRow = TableRow(this)
        addHeaderCell(headerRow, "Note ID")
        addHeaderCell(headerRow, "Book ID")
        addHeaderCell(headerRow, "Bitmap")
        addHeaderCell(headerRow, "PageT")
        tableLayout!!.addView(headerRow)

        // Add data rows
        BackgroundOps.Companion.execute<List<HandwrittenNoteEntity>?>(
            Callable<List<HandwrittenNoteEntity?>?> { handwrittenNotesDao.getAllHandwrittenNotes() },
            Consumer<List<HandwrittenNoteEntity>?> { entries: List<HandwrittenNoteEntity>? ->
                for (entry in entries!!) {
                    val row = TableRow(this)
                    addCell(row, entry.noteId.toString())
                    addCell(row, entry.bookId.toString())
                    addCell(row, entry.bitmapFilePath)
                    addCell(row, entry.pageTemplateFilePath)
                    tableLayout!!.addView(row)
                }
            })
    }

    private fun showNoteAtomicNotes(noteId: Long) {
        tableLayout!!.removeAllViews()

        // Add header
        val headerRow = TableRow(this)
        addHeaderCell(headerRow, "Note ID")
        addHeaderCell(headerRow, "filename")
        addHeaderCell(headerRow, "filepath")
        addHeaderCell(headerRow, "note_type")
        tableLayout!!.addView(headerRow)

        // Add data rows
        BackgroundOps.Companion.execute<List<AtomicNoteEntity>?>(
            Callable<List<AtomicNoteEntity?>?> { atomicNoteEntitiesDao.getAllAtomicNotes() },
            Consumer<List<AtomicNoteEntity?>?> { entries: List<AtomicNoteEntity?>? ->
                if (CollectionUtils.isEmpty(entries)) return@execute
                for (entry in entries!!) {
                    val row = TableRow(this)
                    addCell(row, entry.getNoteId().toString())
                    addCell(row, entry.getFilename())
                    addCell(row, entry.getFilepath())
                    addCell(row, entry.getNoteType())
                    tableLayout!!.addView(row)
                }
            })
    }

    private fun showSmartBooks(noteId: Long) {
        tableLayout!!.removeAllViews()

        // Add header
        val headerRow = TableRow(this)
        addHeaderCell(headerRow, "Book ID")
        addHeaderCell(headerRow, "Title")
        tableLayout!!.addView(headerRow)

        // Add data rows
        BackgroundOps.Companion.execute<List<SmartBookEntity>?>(
            Callable<List<SmartBookEntity?>?> { smartBooksDao.getAllSmartBooks() },
            Consumer<List<SmartBookEntity?>?> { entries: List<SmartBookEntity?>? ->
                if (CollectionUtils.isEmpty(entries)) return@execute
                for (entry in entries!!) {
                    val row = TableRow(this)
                    addCell(row, entry.getBookId().toString())
                    addCell(row, entry.getTitle())
                    tableLayout!!.addView(row)
                }
            })
    }

    private fun showSmartBookPages(noteId: Long) {
        tableLayout!!.removeAllViews()

        // Add header
        val headerRow = TableRow(this)
        addHeaderCell(headerRow, "Book ID")
        addHeaderCell(headerRow, "Note ID")
        tableLayout!!.addView(headerRow)

        // Add data rows
        BackgroundOps.Companion.execute<List<SmartBookPage>?>(
            Callable<List<SmartBookPage?>?> { smartBookPagesDao.getAllSmartBookPages() },
            Consumer<List<SmartBookPage?>?> { entries: List<SmartBookPage?>? ->
                if (CollectionUtils.isEmpty(entries)) return@execute
                for (entry in entries!!) {
                    val row = TableRow(this)
                    addCell(row, entry.getBookId().toString())
                    addCell(row, entry.getNoteId().toString())
                    tableLayout!!.addView(row)
                }
            })
    }


    private fun showTermFrequencyData(noteId: Long) {
        tableLayout!!.removeAllViews()

        // Add header
        val headerRow = TableRow(this)
        addHeaderCell(headerRow, "Note ID")
        addHeaderCell(headerRow, "Term")
        addHeaderCell(headerRow, "Frequency")
        tableLayout!!.addView(headerRow)

        // Add data rows
        BackgroundOps.Companion.execute<List<NoteTermFrequency>?>(
            Callable<List<NoteTermFrequency?>?> { noteTermFrequencyDao.getAllTermFrequencies() },
            Consumer<List<NoteTermFrequency?>?> { entries: List<NoteTermFrequency?>? ->
                if (CollectionUtils.isEmpty(entries)) return@execute
                for (entry in entries!!) {
                    val row = TableRow(this)
                    addCell(row, entry.getNoteId().toString())
                    addCell(row, entry.getTerm())
                    addCell(row, entry.getTermFrequency().toString())
                    tableLayout!!.addView(row)
                }
            })
    }

    private fun showNoteTextData(noteId: Long) {
        tableLayout!!.removeAllViews()

        // Add header
        val headerRow = TableRow(this)
        addHeaderCell(headerRow, "Note ID")
        addHeaderCell(headerRow, "Text")
        tableLayout!!.addView(headerRow)

        // Add data rows
        BackgroundOps.Companion.execute<List<NoteOcrText>?>(
            Callable<List<NoteOcrText?>?> { noteOcrTextDao.getAllNoteText() },
            Consumer<List<NoteOcrText>?> { entries: List<NoteOcrText>? ->
                for (entry in entries!!) {
                    val row = TableRow(this)
                    addCell(row, entry.noteId.toString())
                    addCell(row, entry.extractedText)
                    tableLayout!!.addView(row)
                }
            })
    }

    private fun addHeaderCell(row: TableRow, text: String) {
        val textView = TextView(this)
        textView.text = text
        textView.setPadding(16, 16, 16, 16)
        textView.setTypeface(null, Typeface.BOLD)
        textView.setTextColor(resources.getColor(android.R.color.white))
        row.addView(textView)
    }

    private fun addCell(row: TableRow, text: String) {
        val textView = TextView(this)
        textView.text = text
        textView.setPadding(16, 16, 16, 16)
        textView.gravity = Gravity.CENTER_VERTICAL
        row.addView(textView)
    }

    abstract inner class TableSelector : OnTabSelectedListener {
        abstract override fun onTabSelected(tab: TabLayout.Tab)

        override fun onTabUnselected(tab: TabLayout.Tab) {
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
        }
    }
}