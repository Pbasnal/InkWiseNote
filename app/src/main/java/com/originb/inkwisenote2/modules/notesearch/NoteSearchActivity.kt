package com.originb.inkwisenote2.modules.notesearch

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNoteGridAdapter
import org.koin.android.compat.ViewModelCompat.getViewModel

class NoteSearchActivity : AppCompatActivity() {
    private var searchInput: EditText? = null
    private var searchButton: Button? = null
    private var smartNoteGridAdapter: SmartNoteGridAdapter? = null
    private var viewModel: NoteSearchViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        // 1. Initialize ViewModel with Koin DI
        viewModel = getViewModel<NoteSearchViewModel>(this, NoteSearchViewModel::class.java)

        // 2. Initialize UI Components
        searchInput = findViewById<EditText>(R.id.searchInput)
        searchButton = findViewById<Button>(R.id.searchButton)
        setupRecyclerView()

        // 3. Set up Observers (The "Reactive" part)
        observeViewModel()

        // 4. Input events
        searchButton!!.setOnClickListener(View.OnClickListener { view: View? ->
            viewModel!!.performSearch(
                searchInput!!.getText().toString()
            )
        }
        )
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.note_search_card_grid_view)
        smartNoteGridAdapter = SmartNoteGridAdapter(this, ArrayList<SmartNotebook>(), false)
        recyclerView.setAdapter(smartNoteGridAdapter)
        recyclerView.setHasFixedSize(true)
    }

    private fun observeViewModel() {
        // Update list when search results change
        viewModel!!.searchResults.observe(this) { results: MutableList<SmartNotebook> ->
            smartNoteGridAdapter!!.setSmartNotebooks(results.toMutableList())
        }

        // Show toast when the ViewModel sends a message
        viewModel!!.toastMessage.observe(this, Observer { message: String? ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
