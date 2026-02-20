package com.originb.inkwisenote2.modules.queries.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.queries.data.QueryEntity
import com.originb.inkwisenote2.modules.smarthome.NotesAdapter
import com.originb.inkwisenote2.modules.smarthome.QueryNoteResult
import org.koin.android.compat.ViewModelCompat.getViewModel

class QueryResultsActivity : AppCompatActivity() {
    private var viewModel: QueryResultsViewModel? = null
    private var resultsRecyclerView: RecyclerView? = null
    private var notesAdapter: NotesAdapter? = null
    private var querySpinner: Spinner? = null
    private var emptyStateText: TextView? = null
    private var initialQueryName: String? = null
    private var selectedQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query_results)


        // Check if a specific query was requested
        if (getIntent().hasExtra("query_name")) {
            initialQueryName = getIntent().getStringExtra("query_name")
        }


        // Setup toolbar
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (getSupportActionBar() != null) {
            getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
            getSupportActionBar()!!.setTitle(if (initialQueryName != null) initialQueryName else getString(R.string.query_results_title))
        }


        // Initialize views
        querySpinner = findViewById<Spinner>(R.id.query_spinner)
        resultsRecyclerView = findViewById<RecyclerView>(R.id.results_recycler_view)
        emptyStateText = findViewById<TextView>(R.id.empty_state_text)


        // Setup RecyclerView
        resultsRecyclerView!!.setLayoutManager(LinearLayoutManager(this))
        notesAdapter = NotesAdapter(this)
        resultsRecyclerView!!.setAdapter(notesAdapter)


        // Get ViewModel from Koin
        viewModel = getViewModel<QueryResultsViewModel>(this, QueryResultsViewModel::class.java)


        // If we have an initial query, load its results directly
        if (initialQueryName != null) {
            viewModel!!.loadQueryResults(initialQueryName)
        }


        // Observe queries for dropdown
        viewModel!!.getQueries()
            .observe(this, Observer { queries: MutableList<QueryEntity?>? -> this.setupQuerySpinner(queries) })


        // Observe current query results
        viewModel!!.getCurrentQueryResults()
            .observe(this, Observer { results: MutableSet<QueryNoteResult?>? -> this.displayResults(results!!) })
    }

    private fun setupQuerySpinner(queries: MutableList<QueryEntity>) {
        if (queries.isEmpty()) {
            querySpinner!!.setVisibility(View.GONE)
            emptyStateText!!.setVisibility(View.VISIBLE)
            emptyStateText!!.setText(R.string.no_queries_available)
            return
        }

        val queryNames: MutableList<String?> = ArrayList<String?>()
        for (query in queries) {
            queryNames.add(query.getName())
        }

        val adapter = ArrayAdapter<String?>(
            this, android.R.layout.simple_spinner_item, queryNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        querySpinner!!.setAdapter(adapter)
        querySpinner!!.setVisibility(View.VISIBLE)


        // Set initial selection based on passed query name
        if (initialQueryName != null) {
            val position = queryNames.indexOf(initialQueryName)
            if (position >= 0) {
                querySpinner!!.setSelection(position)
            } else if (!queryNames.isEmpty()) {
                // If the requested query doesn't exist, select the first one
                querySpinner!!.setSelection(0)
                initialQueryName = queryNames.get(0)
                viewModel!!.loadQueryResults(initialQueryName)
            }
        } else if (!queryNames.isEmpty()) {
            // No initial query, select first one
            querySpinner!!.setSelection(0)
            initialQueryName = queryNames.get(0)
            viewModel!!.loadQueryResults(initialQueryName)
        }

        querySpinner!!.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedQuery = queryNames.get(position)
                if (getSupportActionBar() != null) {
                    getSupportActionBar()!!.setTitle(selectedQuery)
                }
                viewModel!!.loadQueryResults(selectedQuery)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        })
    }

    private fun displayResults(results: MutableSet<QueryNoteResult?>) {
        if (results.isEmpty()) {
            resultsRecyclerView!!.setVisibility(View.GONE)
            emptyStateText!!.setVisibility(View.VISIBLE)
            emptyStateText!!.setText(R.string.no_results_found)
        } else {
            resultsRecyclerView!!.setVisibility(View.VISIBLE)
            emptyStateText!!.setVisibility(View.GONE)
            notesAdapter!!.setNotes(selectedQuery, results)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
