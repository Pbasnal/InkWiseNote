package com.originb.inkwisenote2.modules.noterelation.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.DateTimeUtils.msToDateTime
import com.originb.inkwisenote2.common.Routing.HomePageActivity.openSmartHomePageAndStartFresh
import com.originb.inkwisenote2.common.Routing.SmartNotebookActivity.openNotebookIntent
import com.originb.inkwisenote2.modules.noterelation.data.RelatedNotesUiState
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.smartnotes.ui.SmartNoteGridAdapter
import org.koin.android.compat.ViewModelCompat.getViewModel
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate

class RelatedNotesActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var smartNoteGridAdapter: SmartNoteGridAdapter? = null
    private var viewModel: RelatedNotesViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_related_notes)

        // Koin Injection
        viewModel = getViewModel<RelatedNotesViewModel>(this, RelatedNotesViewModel::class.java)

        setupRecyclerView()
        observeViewModel()

        val rootBookId = getIntent().getLongExtra("book_id", 0)
        viewModel!!.loadRelatedNotes(rootBookId)
    }

    private fun observeViewModel() {
        viewModel!!.uiState.observe(this, Observer { state: RelatedNotesUiState? ->
            renderRootNote(state!!)
            smartNoteGridAdapter!!.updateNoteRelations(state.relations)
            smartNoteGridAdapter!!.setSmartNotebooks(state.relatedBooks)
        })

        viewModel!!.noteDeletedEvent.observe(this, Observer { deleted: Boolean? ->
            if (deleted) openSmartHomePageAndStartFresh(this)
        })
    }

    private fun renderRootNote(state: RelatedNotesUiState) {
        val includedCard = findViewById<View>(R.id.main_note_card)
        val cardImage = includedCard.findViewById<ImageView>(R.id.card_image)
        val cardTitle = includedCard.findViewById<TextView>(R.id.card_name)
        val deleteButton = includedCard.findViewById<ImageButton>(R.id.btn_dlt_note)

        state.rootImage.noteImage!!.ifPresent(Consumer { bm: Bitmap? -> cardImage.setImageBitmap(bm) })
        val smartBook = state.rootNotebook.getSmartBook()

        val noteTitle = Optional.ofNullable<String?>(smartBook.getTitle())
            .filter(Predicate { title: String? -> !title!!.trim { it <= ' ' }.isEmpty() })
            .orElse(msToDateTime(smartBook.getLastModifiedTimeMillis()))

        cardTitle.setText(noteTitle)

        cardImage.setOnClickListener(View.OnClickListener { v: View? ->
            openNotebookIntent(
                this,
                getFilesDir().getPath(),
                smartBook.getBookId()
            )
        })

        deleteButton.setOnClickListener(View.OnClickListener { v: View? -> viewModel!!.deleteRootNote(state.rootNotebook) })
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById<RecyclerView>(R.id.related_note_card_grid_view)
        recyclerView!!.setLayoutManager(GridLayoutManager(this, 2))
        smartNoteGridAdapter = SmartNoteGridAdapter(this, ArrayList<SmartNotebook?>(), false)
        recyclerView!!.setAdapter(smartNoteGridAdapter)
    }
}