package com.originb.inkwisenote2.modules.queries.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.originb.inkwisenote2.R;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;

import java.util.ArrayList;

public class QueryCreationActivity extends AppCompatActivity {
    private QueryViewModel viewModel;
    private QueryListAdapter queryListAdapter;

    private EditText currentQueryNameTextView;

    private ChipGroup wordsToFindContainer;
    private EditText wordToFindInput;
    private Button addToFindBtn;

    private ChipGroup wordsToIgnoreContainer;
    private EditText wordToIgnoreInput;
    private Button addToIgnoreBtn;

    private Button saveQueryButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_creation);

        viewModel = new ViewModelProvider(this).get(QueryViewModel.class);

        currentQueryNameTextView = findViewById(R.id.current_query_name);

        wordsToFindContainer = findViewById(R.id.words_to_find_container);
        wordToFindInput = findViewById(R.id.word_to_find_input);
        addToFindBtn = findViewById(R.id.add_to_find);

        wordsToIgnoreContainer = findViewById(R.id.words_to_ignore_container);
        wordToIgnoreInput = findViewById(R.id.word_to_ignore_input);
        addToIgnoreBtn = findViewById(R.id.add_to_ignore);


        saveQueryButton = findViewById(R.id.save_query);

        setupButtons();
        setupObservers();
        setupQueryList();
    }

    private void setupButtons() {

        addToFindBtn.setOnClickListener(v -> {
            viewModel.addWordToFind(wordToFindInput.getText().toString().trim());
            wordToFindInput.setText("");
        });

        addToIgnoreBtn.setOnClickListener(v -> {
            viewModel.addWordToIgnore(wordToIgnoreInput.getText().toString().trim());
            wordToIgnoreInput.setText("");
        });

        saveQueryButton.setOnClickListener(v -> saveQuery());
    }

    private void setupObservers() {
        viewModel.onWordsToFindChange(this, words -> {
            wordsToFindContainer.removeAllViews();
            for (String word : words) {
                Chip chip = createWordChip(word);
                chip.setOnCloseIconClickListener(v -> {
                    viewModel.removeWordToFind(word);
                    wordsToFindContainer.removeView(chip);
                });
                wordsToFindContainer.addView(chip);
            }
        });

        viewModel.onWordsToIgnoreChange(this, words -> {
            wordsToIgnoreContainer.removeAllViews();
            for (String word : words) {
                Chip chip = createWordChip(word);
                chip.setOnCloseIconClickListener(v -> {
                    viewModel.removeWordToIgnore(word);
                    wordsToIgnoreContainer.removeView(chip);
                });
                wordsToIgnoreContainer.addView(chip);
            }
        });

        viewModel.onQueryNameChange(this, name -> {
            currentQueryNameTextView.setText(name != null ? name : "");
        });
    }

    private Chip createWordChip(String word) {
        Chip chip = new Chip(this);
        chip.setText(word);
        chip.setCloseIconVisible(true);

        return chip;
    }

    private void saveQuery() {
        String queryName = currentQueryNameTextView.getText().toString().trim();
        if (queryName.isEmpty()) queryName = "untitled query";

        final String newQueryName = queryName; // needed because lambda needs final
        viewModel.findQueryWithQueryName(queryName, query -> {
            if (query == null) {
                viewModel.saveQuery(newQueryName);
            } else {
                showErrorDialogMsg("Error",
                        "Query name " + newQueryName + " already exists\nChoose a different name");
            }
        });
    }

    private void showErrorDialogMsg(String title, String message) {
        TextView textView = new TextView(this);
        textView.setText(message);

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(textView)
                .setPositiveButton("Ok", null)
                .show();
    }

    private void setupQueryList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_all_queries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        queryListAdapter = new QueryListAdapter(new QueryListAdapter.OnQueryClickListener() {
            @Override
            public void onQueryClick(QueryEntity query) {
                // Handle regular click if needed
            }

            @Override
            public void onEditClick(QueryEntity query) {
                viewModel.loadQuery(query);
            }

            @Override
            public void onDeleteClick(QueryEntity query) {
                viewModel.deleteQuery(query);
            }
        });

        recyclerView.setAdapter(queryListAdapter);

        viewModel.getAllQueries().observe(this, queries -> {
            queryListAdapter.submitList(new ArrayList(queries.values()));
        });
    }
} 