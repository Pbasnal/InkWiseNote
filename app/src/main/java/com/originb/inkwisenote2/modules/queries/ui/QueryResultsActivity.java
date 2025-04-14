package com.originb.inkwisenote2.modules.queries.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;
import com.originb.inkwisenote2.modules.smarthome.NotesAdapter;
import com.originb.inkwisenote2.modules.smarthome.QueryNoteResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class QueryResultsActivity extends AppCompatActivity {
    
    private QueryResultsViewModel viewModel;
    private RecyclerView resultsRecyclerView;
    private NotesAdapter notesAdapter;
    private Spinner querySpinner;
    private TextView emptyStateText;
    private String initialQueryName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_results);
        
        // Check if a specific query was requested
        if (getIntent().hasExtra("query_name")) {
            initialQueryName = getIntent().getStringExtra("query_name");
        }
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(initialQueryName != null ? 
                    initialQueryName : getString(R.string.query_results_title));
        }
        
        // Initialize views
        querySpinner = findViewById(R.id.query_spinner);
        resultsRecyclerView = findViewById(R.id.results_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        
        // Setup RecyclerView
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesAdapter = new NotesAdapter(this);
        resultsRecyclerView.setAdapter(notesAdapter);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(QueryResultsViewModel.class);
        
        // If we have an initial query, load its results directly
        if (initialQueryName != null) {
            viewModel.loadQueryResults(initialQueryName);
        }
        
        // Observe queries for dropdown
        viewModel.getQueries().observe(this, this::setupQuerySpinner);
        
        // Observe current query results
        viewModel.getCurrentQueryResults().observe(this, this::displayResults);
    }
    
    private void setupQuerySpinner(List<QueryEntity> queries) {
        if (queries.isEmpty()) {
            querySpinner.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText(R.string.no_queries_available);
            return;
        }
        
        List<String> queryNames = new ArrayList<>();
        for (QueryEntity query : queries) {
            queryNames.add(query.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, queryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        querySpinner.setAdapter(adapter);
        querySpinner.setVisibility(View.VISIBLE);
        
        // Set initial selection based on passed query name
        if (initialQueryName != null) {
            int position = queryNames.indexOf(initialQueryName);
            if (position >= 0) {
                querySpinner.setSelection(position);
            } else if (!queryNames.isEmpty()) {
                // If the requested query doesn't exist, select the first one
                querySpinner.setSelection(0);
                initialQueryName = queryNames.get(0);
                viewModel.loadQueryResults(initialQueryName);
            }
        } else if (!queryNames.isEmpty()) {
            // No initial query, select first one
            querySpinner.setSelection(0);
            initialQueryName = queryNames.get(0);
            viewModel.loadQueryResults(initialQueryName);
        }
        
        querySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedQuery = queryNames.get(position);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(selectedQuery);
                }
                viewModel.loadQueryResults(selectedQuery);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void displayResults(Set<QueryNoteResult> results) {
        if (results.isEmpty()) {
            resultsRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText(R.string.no_results_found);
        } else {
            resultsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
            notesAdapter.setNotes(results);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
