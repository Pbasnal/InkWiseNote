package com.originb.inkwisenote2.modules.queries.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.queries.data.QueryEntity
import com.originb.inkwisenote2.modules.queries.ui.QueryListAdapter.OnQueryClickListener
import org.koin.android.compat.ViewModelCompat.getViewModel

class QueryCreationActivity : AppCompatActivity() {
    private var viewModel: QueryViewModel? = null
    private var queryListAdapter: QueryListAdapter? = null

    private var currentQueryNameTextView: EditText? = null

    private var wordsToFindContainer: ChipGroup? = null
    private var wordToFindInput: EditText? = null
    private var addToFindBtn: Button? = null

    private var wordsToIgnoreContainer: ChipGroup? = null
    private var wordToIgnoreInput: EditText? = null
    private var addToIgnoreBtn: Button? = null

    private var saveQueryButton: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query_creation)

        // Get ViewModel from Koin
        viewModel = getViewModel<QueryViewModel>(this, QueryViewModel::class.java)

        currentQueryNameTextView = findViewById<EditText>(R.id.current_query_name)

        wordsToFindContainer = findViewById<ChipGroup>(R.id.words_to_find_container)
        wordToFindInput = findViewById<EditText>(R.id.word_to_find_input)
        addToFindBtn = findViewById<Button>(R.id.add_to_find)

        wordsToIgnoreContainer = findViewById<ChipGroup>(R.id.words_to_ignore_container)
        wordToIgnoreInput = findViewById<EditText>(R.id.word_to_ignore_input)
        addToIgnoreBtn = findViewById<Button>(R.id.add_to_ignore)


        saveQueryButton = findViewById<Button>(R.id.save_query)

        setupButtons()
        setupObservers()
        setupQueryList()
    }

    private fun setupButtons() {
        addToFindBtn!!.setOnClickListener(View.OnClickListener { v: View? ->
            viewModel!!.addWordToFind(wordToFindInput!!.getText().toString().trim { it <= ' ' })
            wordToFindInput!!.setText("")
        })

        addToIgnoreBtn!!.setOnClickListener(View.OnClickListener { v: View? ->
            viewModel!!.addWordToIgnore(wordToIgnoreInput!!.getText().toString().trim { it <= ' ' })
            wordToIgnoreInput!!.setText("")
        })

        saveQueryButton!!.setOnClickListener(View.OnClickListener { v: View? -> saveQuery() })
    }

    private fun setupObservers() {
        viewModel!!.onWordsToFindChange(this, Observer { words: MutableList<String?>? ->
            wordsToFindContainer!!.removeAllViews()
            for (word in words!!) {
                val chip = createWordChip(word)
                chip.setOnCloseIconClickListener(View.OnClickListener { v: View? ->
                    viewModel!!.removeWordToFind(word)
                    wordsToFindContainer!!.removeView(chip)
                })
                wordsToFindContainer!!.addView(chip)
            }
        })

        viewModel!!.onWordsToIgnoreChange(this, Observer { words: MutableList<String?>? ->
            wordsToIgnoreContainer!!.removeAllViews()
            for (word in words!!) {
                val chip = createWordChip(word)
                chip.setOnCloseIconClickListener(View.OnClickListener { v: View? ->
                    viewModel!!.removeWordToIgnore(word)
                    wordsToIgnoreContainer!!.removeView(chip)
                })
                wordsToIgnoreContainer!!.addView(chip)
            }
        })

        viewModel!!.onQueryNameChange(this, Observer { name: String? ->
            currentQueryNameTextView!!.setText(if (name != null) name else "")
        })
    }

    private fun createWordChip(word: String?): Chip {
        val chip = Chip(this)
        chip.setText(word)
        chip.setCloseIconVisible(true)

        return chip
    }

    private fun saveQuery() {
        var queryName = currentQueryNameTextView!!.getText().toString().trim { it <= ' ' }
        if (queryName.isEmpty()) queryName = "untitled query"

        val newQueryName = queryName // needed because lambda needs final
        viewModel!!.findQueryWithQueryName(queryName, Observer { query: QueryEntity? ->
            if (query == null) {
                viewModel!!.saveQuery(newQueryName)
            } else {
                showErrorDialogMsg(
                    "Error",
                    "Query name " + newQueryName + " already exists\nChoose a different name"
                )
            }
        })
    }

    private fun showErrorDialogMsg(title: String?, message: String?) {
        val textView = TextView(this)
        textView.setText(message)

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(textView)
            .setPositiveButton("Ok", null)
            .show()
    }

    private fun setupQueryList() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_all_queries)
        recyclerView.setLayoutManager(LinearLayoutManager(this))

        queryListAdapter = QueryListAdapter(object : OnQueryClickListener {
            override fun onQueryClick(query: QueryEntity?) {
                // Handle regular click if needed
            }

            override fun onEditClick(query: QueryEntity) {
                viewModel!!.loadQuery(query)
            }

            override fun onDeleteClick(query: QueryEntity?) {
                viewModel!!.deleteQuery(query)
            }
        })

        recyclerView.setAdapter(queryListAdapter)

        viewModel!!.getAllQueries().observe(this, Observer { queries: MutableMap<String?, QueryEntity?>? ->
            queryListAdapter!!.submitList(
                ArrayList<Any?>(queries!!.values)
            )
        })
    }
}