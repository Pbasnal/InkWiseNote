package com.originb.inkwisenote2.modules.smartnotes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.Strings
import com.originb.inkwisenote2.common.Strings.isNotEmpty
import com.originb.inkwisenote2.common.Strings.isNullOrWhitespace
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

/**
 * Fragment for displaying and editing text notes
 */
class TextNoteFragment(
    smartNotebook: SmartNotebook?, atomicNote: AtomicNoteEntity?, private val textNotesDao: TextNotesDao,
// Additional dependencies for NoteDebugDialog
    private val handwrittenNoteRepository: HandwrittenNoteRepository?, private val noteOcrTextDao: NoteOcrTextsDao?,
    private val smartNotebookRepository: SmartNotebookRepository?
) : NoteFragment(smartNotebook, atomicNote) {
    private var noteEditText: EditText? = null
    private var deleteBtn: ImageButton? = null
    private var debugButton: ImageButton? = null
    private var textNoteEntity: TextNoteEntity? = null
    private var markdownFile: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreate(savedInstanceState)

        // Inflate the layout for this fragment
        val itemView = inflater.inflate(R.layout.note_text_fragment, container, false)
        noteEditText = itemView.findViewById<EditText?>(R.id.note_edit_text)
        deleteBtn = itemView.findViewById<ImageButton>(R.id.delete_note)
        debugButton = itemView.findViewById<ImageButton>(R.id.debug_button)

        return itemView
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        deleteBtn!!.setOnClickListener(View.OnClickListener { view: View? -> confirmDeleteNote() }
        )

        debugButton!!.setOnClickListener(View.OnClickListener { v: View? ->
            showDebugDialog()
        })

        loadNote()
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

    protected fun loadNote() {
        execute(Runnable {
            // Check for markdown file first
            var markdownContent = loadMarkdownFile()
            // Get or create the text note entity for metadata
            textNoteEntity = textNotesDao.getTextNoteForNote(atomicNote.getNoteId())
            if (textNoteEntity == null) {
                textNoteEntity = TextNoteEntity(atomicNote.getNoteId(), smartNotebook.smartBook!!.getBookId())
                textNotesDao.insertTextNote(textNoteEntity)
            }
            if (Strings.isNotEmpty(markdownContent!!) && markdownContent != textNoteEntity!!.getNoteText()) {
                textNoteEntity!!.setNoteText(markdownContent)
                textNotesDao.updateTextNote(textNoteEntity)
            } else if (isNullOrWhitespace(markdownContent)
                && isNotEmpty(textNoteEntity!!.getNoteText())
            ) {
                markdownContent = textNoteEntity!!.getNoteText()
            }
            markdownContent
        }, Runnable { noteText ->
            if (noteEditText != null) {
                noteEditText.setText(noteText)
            }
        })
    }

    /**
     * Load text from markdown file if it exists
     */
    private fun loadMarkdownFile(): String? {
        if (getContext() == null || isNullOrWhitespace(atomicNote.getFilepath())) return null

        // Create markdown file path using notebook directory structure
        val markdownPath = this.markdownFilePath
        markdownFile = File(markdownPath)

        if (!markdownFile!!.exists()) {
            return null
        }

        val content = StringBuilder()
        try {
            BufferedReader(FileReader(markdownFile)).use { reader ->
                var line: String?
                while ((reader.readLine().also { line = it }) != null) {
                    content.append(line).append("\n")
                }
                if (content.length > 0) {
                    content.deleteCharAt(content.length - 1)
                }
                return content.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error reading note file", Toast.LENGTH_SHORT).show()
            }
            return null
        }
    }

    override fun getNoteHolderData(): NoteHolderData {
        val text = if (noteEditText != null) noteEditText!!.getText().toString().trim { it <= ' ' } else ""
        return NoteHolderData.Companion.textNoteData(text)
    }

    private val markdownFilePath: String
        get() = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".md"
}
