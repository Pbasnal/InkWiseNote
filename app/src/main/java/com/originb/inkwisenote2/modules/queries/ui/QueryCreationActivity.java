package com.originb.inkwisenote2.modules.queries.ui;

import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.originb.inkwisenote2.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;
import android.widget.TextView;

import java.util.ArrayList;

public class QueryCreationActivity extends AppCompatActivity {
    private QueryViewModel viewModel;
    private EditText wordInput;
    private ChipGroup wordsToFindContainer;
    private ChipGroup wordsToIgnoreContainer;
    private QueryListAdapter queryListAdapter;
    private boolean isEditing = false;
    private String currentQueryName = "";
    private TextView currentQueryNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_creation);


        viewModel = new ViewModelProvider(this).get(QueryViewModel.class);
        setupViews();
        setupObservers();
        setupQueryList();
    }

    private void setupViews() {
        wordInput = findViewById(R.id.word_input);
        wordsToFindContainer = findViewById(R.id.words_to_find_container);
        wordsToIgnoreContainer = findViewById(R.id.words_to_ignore_container);
        currentQueryNameTextView = findViewById(R.id.current_query_name);

        findViewById(R.id.add_to_find).setOnClickListener(v -> {
            String word = wordInput.getText().toString().trim();
            if (!word.isEmpty()) {
                viewModel.addWordToFind(word);
                wordInput.setText("");
            }
        });

        findViewById(R.id.add_to_ignore).setOnClickListener(v -> {
            String word = wordInput.getText().toString().trim();
            if (!word.isEmpty()) {
                viewModel.addWordToIgnore(word);
                wordInput.setText("");
            }
        });

        findViewById(R.id.save_query).setOnClickListener(v -> showSaveDialog());

        currentQueryNameTextView.setText(currentQueryName);
    }

    private void setupObservers() {
        viewModel.getWordsToFind().observe(this, words -> {
            wordsToFindContainer.removeAllViews();
            for (String word : words) {
                addWordChip(wordsToFindContainer, word, () ->
                        viewModel.removeWordToFind(word));
            }
        });

        viewModel.getWordsToIgnore().observe(this, words -> {
            wordsToIgnoreContainer.removeAllViews();
            for (String word : words) {
                addWordChip(wordsToIgnoreContainer, word, () ->
                        viewModel.removeWordToIgnore(word));
            }
        });

        viewModel.getCurrentQueryName().observe(this, name -> {
            currentQueryNameTextView.setText(name != null ? name : "");
        });
    }

    private void addWordChip(ChipGroup container, String word, Runnable onDelete) {
        Chip chip = new Chip(this);
        chip.setText(word);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            onDelete.run();
            container.removeView(chip);
        });
        container.addView(chip);
    }

    private void showSaveDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter query name");
        
        String existingName = currentQueryNameTextView.getText().toString();
        if (!existingName.isEmpty()) {
            input.setText(existingName);
        }

        new MaterialAlertDialogBuilder(this)
            .setTitle(existingName.isEmpty() ? "Save Query" : "Update Query")
            .setView(input)
            .setPositiveButton(existingName.isEmpty() ? "Save" : "Update", (dialog, which) -> {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    viewModel.saveQuery(name);
                }
            })
            .setNegativeButton("Cancel", null)
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
                isEditing = true;
                currentQueryName = query.getName();
                viewModel.loadQuery(query);
                currentQueryNameTextView.setText(currentQueryName);
            }
        });
        
        recyclerView.setAdapter(queryListAdapter);

        viewModel.getAllQueries().observe(this, queries -> {
            queryListAdapter.submitList(new ArrayList(queries.values()));
        });
    }
} 