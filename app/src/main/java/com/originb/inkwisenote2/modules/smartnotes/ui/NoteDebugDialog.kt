package com.originb.inkwisenote2.modules.smartnotes.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.DateTimeUtils.msToDateTime
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.stream.Collectors

/**
 * Dialog for displaying debug information about a note
 */
class NoteDebugDialog(
    context: Context, private val atomicNote: AtomicNoteEntity?, private val currentSmartNotebook: SmartNotebook?,
    private val smartNotebookRepository: SmartNotebookRepository, private val textNotesDao: TextNotesDao,
    private val noteOcrTextDao: NoteOcrTextsDao, private val handwrittenNoteRepository: HandwrittenNoteRepository?
) : Dialog(context) {
    private var noteInfoTable: TableLayout? = null
    private var relatedNotesTable: TableLayout? = null
    private var smartbooksTable: TableLayout? = null
    private var parsedTextContent: TextView? = null
    private var markdownStrokesContent: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_note_debug_info)

        // Set dialog to use full width and almost full height
        if (getWindow() != null) {
            getWindow()!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            getWindow()!!.setGravity(Gravity.CENTER)
        }

        // Initialize tables
        noteInfoTable = findViewById<TableLayout>(R.id.tableNoteInfo)
        relatedNotesTable = findViewById<TableLayout>(R.id.relatedNotesTable)
        smartbooksTable = findViewById<TableLayout>(R.id.smartbooksTable)
        parsedTextContent = findViewById<TextView>(R.id.parsedTextContent)
        markdownStrokesContent = findViewById<TextView>(R.id.markdownStrokesContent)

        // Set close button click listener
        val closeButton = findViewById<Button>(R.id.close_button)
        closeButton.setOnClickListener(View.OnClickListener { v: View? -> dismiss() })

        // Load note info if available
        if (atomicNote != null) {
            loadNoteInfo()
        }
    }

    private fun loadNoteInfo() {
        // Load basic note info
        addBasicNoteInfo()

        // Add loading indicator to tables before background loading
        addRowToTable(relatedNotesTable!!, "Loading related notes...", "")
        addRowToTable(smartbooksTable!!, "Loading smartbooks...", "")
        parsedTextContent!!.setText(getContext().getString(R.string.loading_parsed_text))
        markdownStrokesContent!!.setText(getContext().getString(R.string.loading_markdown_strokes))

        // Load related notes and smartbooks in background
        execute(Runnable { this.collectDebugData() }, Runnable { this.updateDebugUI() })
    }

    private fun collectDebugData(): DebugData {
        val data = DebugData()

        // Get related notes (notes in the same smartbook)
        if (currentSmartNotebook != null) {
            data.relatedNotes = ArrayList<Any?>(currentSmartNotebook.getAtomicNotes())
            // Remove the current note
            data.relatedNotes!!.remove(atomicNote!!)
        }

        // Get all smartbooks containing this note
        data.smartbooks = smartNotebookRepository.getSmartNotebookContainingNote(atomicNote!!.getNoteId())
            .stream().map<Any?>(SmartNotebook::getSmartBook)
            .collect(Collectors.toList())

        val noteType: NoteType = NoteType.Companion.fromString(atomicNote.getNoteType())
        when (noteType) {
            NoteType.TEXT_NOTE -> {
                // Get parsed text if it's a text note
                val textNote = textNotesDao.getTextNoteForNote(atomicNote.getNoteId())
                if (textNote != null) {
                    data.parsedText = textNote.getNoteText()
                }
            }

            NoteType.HANDWRITTEN_PNG -> {
                // Get parsed text if it's a text note
                val noteOcrText = noteOcrTextDao.readTextFromDb(atomicNote.getNoteId())
                if (noteOcrText != null) {
                    data.parsedText = noteOcrText.extractedText
                }


                // Get markdown strokes content
                data.markdownContent = readMarkdownFile(atomicNote)
            }

            else -> data.parsedText = "Note doesn't have text"
        }

        return data
    }

    /**
     * Reads the markdown file containing strokes data
     * @param note The note entity to read markdown for
     * @return The markdown file content or a message if not found
     */
    private fun readMarkdownFile(note: AtomicNoteEntity): String {
        val markdownPath = note.getFilepath() + "/" + note.getFilename() + ".md"
        val file = File(markdownPath)

        if (!file.exists() || !file.isFile()) {
            return "No markdown file found at: " + markdownPath
        }

        try {
            val content = StringBuilder()
            BufferedReader(FileReader(file)).use { reader ->
                var line: String?
                while ((reader.readLine().also { line = it }) != null) {
                    content.append(line).append("\n")
                }
            }
            return content.toString()
        } catch (e: IOException) {
            return "Error reading markdown file: " + e.message
        }
    }

    private fun updateDebugUI(data: DebugData) {
        // Clear existing tables
        relatedNotesTable!!.removeAllViews()
        smartbooksTable!!.removeAllViews()

        // Update related notes table
        if (data.relatedNotes != null && !data.relatedNotes!!.isEmpty()) {
            for (relatedNote in data.relatedNotes) {
                addRowToTable(relatedNotesTable!!, "Note ID", relatedNote.getNoteId().toString())
                addRowToTable(relatedNotesTable!!, "Note Type", relatedNote.getNoteType())
                addRowToTable(relatedNotesTable!!, "Created", msToDateTime(relatedNote.getCreatedTimeMillis()))
                addRowToTable(relatedNotesTable!!, "Modified", msToDateTime(relatedNote.getLastModifiedTimeMillis()))
                addSeparatorToTable(relatedNotesTable!!)
            }
        } else {
            addRowToTable(relatedNotesTable!!, "No related notes found", "")
        }

        // Update smartbooks table
        if (data.smartbooks != null && !data.smartbooks!!.isEmpty()) {
            for (smartbook in data.smartbooks) {
                addRowToTable(smartbooksTable!!, "Book ID", smartbook.getBookId().toString())
                addRowToTable(smartbooksTable!!, "Title", smartbook.getTitle())
                addRowToTable(smartbooksTable!!, "Created", msToDateTime(smartbook.getCreatedTimeMillis()))
                addRowToTable(smartbooksTable!!, "Modified", msToDateTime(smartbook.getLastModifiedTimeMillis()))
                addSeparatorToTable(smartbooksTable!!)
            }
        } else {
            addRowToTable(smartbooksTable!!, "No smartbooks found", "")
        }

        // Update parsed text
        if (data.parsedText != null && !data.parsedText!!.isEmpty()) {
            parsedTextContent!!.setText(data.parsedText)
        } else {
            parsedTextContent!!.setText(getContext().getString(R.string.no_parsed_text))
        }


        // Update markdown strokes content
        if (data.markdownContent != null && !data.markdownContent!!.isEmpty()) {
            markdownStrokesContent!!.setText(data.markdownContent)
        } else {
            markdownStrokesContent!!.setText(getContext().getString(R.string.no_markdown_strokes))
        }
    }

    private fun addBasicNoteInfo() {
        addRowToTable(noteInfoTable!!, "Note ID", atomicNote!!.getNoteId().toString())
        addRowToTable(noteInfoTable!!, "Note Type", atomicNote.getNoteType())
        addRowToTable(noteInfoTable!!, "Created", msToDateTime(atomicNote.getCreatedTimeMillis()))
        addRowToTable(noteInfoTable!!, "Last Modified", msToDateTime(atomicNote.getLastModifiedTimeMillis()))
        addRowToTable(
            noteInfoTable!!,
            "Working Note Path",
            atomicNote.getFilepath() + "/" + atomicNote.getFilename()
        )


        // Add markdown file path if it's a handwritten note
        if (NoteType.HANDWRITTEN_PNG.name == atomicNote.getNoteType()) {
            val markdownPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".md"
            val markdownFile = File(markdownPath)
            val markdownStatus = if (markdownFile.exists()) "Exists" else "Not created yet"
            addRowToTable(noteInfoTable!!, "Markdown File", markdownPath + " (" + markdownStatus + ")")
        }
    }

    private fun addRowToTable(table: TableLayout, key: String?, value: String?) {
        val row = TableRow(getContext())
        row.setLayoutParams(
            TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // Create key TextView
        val keyView = TextView(getContext())
        keyView.setText(key)
        keyView.setPadding(8, 8, 16, 8)
        keyView.setTextColor(getContext().getResources().getColor(android.R.color.black))
        // Set layout params with weight
        val keyParams = TableRow.LayoutParams(
            0, TableRow.LayoutParams.WRAP_CONTENT, 0.3f
        )
        keyView.setLayoutParams(keyParams)
        keyView.setGravity(Gravity.START or Gravity.TOP)

        // Create value TextView
        val valueView = TextView(getContext())
        valueView.setText(value)
        valueView.setPadding(16, 8, 8, 8)
        // Set layout params with weight
        val valueParams = TableRow.LayoutParams(
            0, TableRow.LayoutParams.WRAP_CONTENT, 0.7f
        )
        valueView.setLayoutParams(valueParams)
        valueView.setGravity(Gravity.START or Gravity.TOP)
        valueView.setSingleLine(false)

        // Add views to row
        row.addView(keyView)
        row.addView(valueView)

        // Add row to table
        table.addView(row)
    }

    private fun addSeparatorToTable(table: TableLayout) {
        val row = TableRow(getContext())
        row.setLayoutParams(
            TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val separator = View(getContext())
        separator.setBackgroundColor(getContext().getResources().getColor(android.R.color.darker_gray))

        val params = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT, 1
        )
        params.span = 2
        params.topMargin = 4
        params.bottomMargin = 4
        separator.setLayoutParams(params)

        row.addView(separator)
        table.addView(row)
    }

    /**
     * Class to hold debug data collected in the background
     */
    private class DebugData {
        var relatedNotes: MutableList<AtomicNoteEntity>? = null
        var smartbooks: MutableList<SmartBookEntity>? = null
        var parsedText: String? = null
        var markdownContent: String? = null
    }
}