package com.originb.inkwisenote2.modules.smartnotes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.isNotEmpty
import com.originb.inkwisenote2.common.isNullOrWhitespace
import com.originb.inkwisenote2.common.showDebugDialog
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
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
open class TextNoteFragment(
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
        // Inflate the layout for this fragment
        val itemView = inflater.inflate(R.layout.note_text_fragment, container, false)
        noteEditText = itemView.findViewById<EditText?>(R.id.note_edit_text)
        deleteBtn = itemView.findViewById<ImageButton>(R.id.delete_note)
        debugButton = itemView.findViewById<ImageButton>(R.id.debug_button)

        return itemView
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        deleteBtn!!.setOnClickListener { confirmDeleteNote() }

        debugButton!!.setOnClickListener { _: View? ->
            showDebugDialog(
                context, atomicNote, smartNotebook,
                smartNotebookRepository, textNotesDao, noteOcrTextDao, handwrittenNoteRepository
            )
        }

        loadNote()
    }


    protected fun loadNote() {
        val note = atomicNote ?: return
        BackgroundOps.execute(
            {
                var markdownContent = loadMarkdownFile()
                textNoteEntity = textNotesDao.getTextNoteForNote(note.noteId)
                val entity = textNoteEntity!!
                if (isNotEmpty(markdownContent) && markdownContent != entity.noteText) {
                    entity.noteText = markdownContent
                    textNotesDao.updateTextNote(entity)
                } else if (isNullOrWhitespace(markdownContent) && isNotEmpty(entity.noteText)) {
                    markdownContent = entity.noteText ?: ""
                }
                markdownContent
            },
            { noteText ->
                noteEditText?.setText(noteText)
            }
        )
    }

    /**
     * Load text from markdown file if it exists
     */
    private fun loadMarkdownFile(): String {
        val note = atomicNote ?: return ""
        if (isNullOrWhitespace(note.filepath)) return ""

        // Create markdown file path using notebook directory structure
        val markdownPath = note.filepath + "/" + (note.filename ?: "") + ".md"
        markdownFile = File(markdownPath)

        if (!markdownFile!!.exists()) {
            return ""
        }

        val content = StringBuilder()
        try {
            BufferedReader(FileReader(markdownFile!!)).use { reader ->
                var line: String?
                while ((reader.readLine().also { line = it }) != null) {
                    content.append(line).append("\n")
                }
                if (content.isNotEmpty()) {
                    content.deleteCharAt(content.length - 1)
                }
                return content.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            context?.let { Toast.makeText(it, "Error reading note file", Toast.LENGTH_SHORT).show() }
            return ""
        }
    }

    override val noteHolderData: NoteHolderData
        get() {
            val text = noteEditText?.text?.toString()?.trim() ?: ""
            return NoteHolderData.textNoteData(text)
        }
}
