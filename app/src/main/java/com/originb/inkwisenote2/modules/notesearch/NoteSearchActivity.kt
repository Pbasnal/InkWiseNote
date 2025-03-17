package com.originb.inkwisenote2.modules.notesearch

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNoteGridAdapter
import java.util.concurrent.Callable
import java.util.function.Consumer
import java.util.stream.Collectors

class NoteSearchActivity : AppCompatActivity() {
    private var searchInput: EditText? = null
    private var searchButton: Button? = null
    private var resultsList: MutableList<SmartNotebook>? = null

    private var smartNoteGridAdapter: SmartNoteGridAdapter? = null

    private var noteOcrTextDao: NoteOcrTextDao? = null
    private var smartNotebookRepository: SmartNotebookRepository? = null

    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        noteOcrTextDao = Repositories.Companion.getInstance().getNotesDb().noteOcrTextDao()
        smartNotebookRepository = Repositories.Companion.getInstance().getSmartNotebookRepository()

        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)

        createGridLayoutToShowNotes()

        searchButton.setOnClickListener(View.OnClickListener { performSearch(searchInput.getText().toString()) })
    }

    fun createGridLayoutToShowNotes() {
        resultsList = ArrayList()

        recyclerView = findViewById(R.id.note_search_card_grid_view)
        smartNoteGridAdapter = SmartNoteGridAdapter(this, ArrayList())

        recyclerView.setAdapter(smartNoteGridAdapter)
        recyclerView.setHasFixedSize(true)
    }

    private fun performSearch(query: String) {
        // Filter results based on query
        if (query.length < 3) {
            Toast.makeText(this, "enter at least 3 characters to search", Toast.LENGTH_SHORT).show()
            return
        }

        resultsList!!.clear()
        BackgroundOps.Companion.execute<Set<SmartNotebook>?>(
            Callable<Set<SmartNotebook?>?> {
                val smartNotebooks = smartNotebookRepository!!.getSmartNotebooks(query)
                val noteOcrs = noteOcrTextDao!!.searchTextFromDb(query)
                if (noteOcrs != null && !noteOcrs.isEmpty()) {
                    val noteIds = noteOcrs.stream()
                        .map { obj: NoteOcrText? -> obj.getNoteId() }
                        .collect(Collectors.toSet())
                    smartNotebooks!!.addAll(smartNotebookRepository!!.getSmartNotebooksForNoteIds(noteIds))
                }
                smartNotebooks
            },
            Consumer<Set<SmartNotebook>?> { smartNotebooks: Set<SmartNotebook>? ->
                resultsList!!.addAll(smartNotebooks!!)
                smartNoteGridAdapter!!.setSmartNotebooks(resultsList)
            })
    }
}
