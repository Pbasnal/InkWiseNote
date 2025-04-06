package com.originb.inkwisenote2.modules.queries.ui;

import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.originb.inkwisenote2.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class QueryCreationActivity extends AppCompatActivity {
    private QueryViewModel viewModel;
    private EditText wordInput;
    private ChipGroup wordsToFindContainer;
    private ChipGroup wordsToIgnoreContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_creation);

        viewModel = new ViewModelProvider(this).get(QueryViewModel.class);
        setupViews();
        setupObservers();
    }

    private void setupViews() {
        wordInput = findViewById(R.id.word_input);
        wordsToFindContainer = findViewById(R.id.words_to_find_container);
        wordsToIgnoreContainer = findViewById(R.id.words_to_ignore_container);

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
    }

    private void addWordChip(ChipGroup container, String word, Runnable onDelete) {
        Chip chip = new Chip(this);
        chip.setText(word);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> onDelete.run());
        container.addView(chip);
    }

    private void showSaveDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter query name");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Save Query")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        viewModel.saveQuery(name);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
} 