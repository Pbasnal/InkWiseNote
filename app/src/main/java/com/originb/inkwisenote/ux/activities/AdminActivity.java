package com.originb.inkwisenote.ux.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.tabs.TabLayout;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.data.dao.NoteOcrTextDao;
import com.originb.inkwisenote.data.dao.NoteTermFrequencyDao;
import com.originb.inkwisenote.data.entities.notedata.NoteOcrText;
import com.originb.inkwisenote.data.entities.notedata.NoteTermFrequency;
import com.originb.inkwisenote.modules.messaging.BackgroundOps;
import com.originb.inkwisenote.modules.repositories.Repositories;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class AdminActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private NoteTermFrequencyDao noteTermFrequencyDao;
    private NoteOcrTextDao noteOcrTextDao;
    private EditText editText;
    private Button filterQueryBtn ;
    private String selectedTab = "Term Frequencies";

    private Map<String, Consumer<Long>> tablePopulators = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        editText = findViewById(R.id.note_id_query);
        filterQueryBtn = findViewById(R.id.filter_query);
        tableLayout = findViewById(R.id.data_table);
        noteTermFrequencyDao = Repositories.getInstance().getNotesDb().noteTermFrequencyDao();
        noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();

        tablePopulators.put("Term Frequencies", this::showTermFrequencyData);
        tablePopulators.put("Note Text", this::showNoteTextData);

        TabLayout tabLayout = findViewById(R.id.table_selector_tabs);
        tabLayout.addOnTabSelectedListener(new TableSelector() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Long noteId = Long.parseLong(editText.getText().toString());
                selectedTab = tab.getText().toString();
                tablePopulators.get(selectedTab).accept(noteId);
            }
        });

        // Show term frequency data by default
        showTermFrequencyData(0L);

        filterQueryBtn.setOnClickListener((btn) -> {
            Long noteId = Long.parseLong(editText.getText().toString());
            tablePopulators.get(selectedTab).accept(noteId);
        });
    }

    private void showTermFrequencyData(Long noteId) {
        tableLayout.removeAllViews();

        // Add header
        TableRow headerRow = new TableRow(this);
        addHeaderCell(headerRow, "Note ID");
        addHeaderCell(headerRow, "Term");
        addHeaderCell(headerRow, "Frequency");
        tableLayout.addView(headerRow);

        // Add data rows
        BackgroundOps.execute(() -> noteTermFrequencyDao.getAllTermFrequencies(), entries -> {

            if (CollectionUtils.isEmpty(entries)) return;

            for (NoteTermFrequency entry : entries) {
                TableRow row = new TableRow(this);
                addCell(row, String.valueOf(entry.getNoteId()));
                addCell(row, entry.getTerm());
                addCell(row, String.valueOf(entry.getTermFrequency()));
                tableLayout.addView(row);
            }
        });
    }

    private void showNoteTextData(Long noteId) {
        tableLayout.removeAllViews();

        // Add header
        TableRow headerRow = new TableRow(this);
        addHeaderCell(headerRow, "Note ID");
        addHeaderCell(headerRow, "Text");
        tableLayout.addView(headerRow);

        // Add data rows
        BackgroundOps.execute(() -> noteOcrTextDao.getAllNoteText(), entries -> {
            for (NoteOcrText entry : entries) {
                TableRow row = new TableRow(this);
                addCell(row, String.valueOf(entry.getNoteId()));
                addCell(row, entry.getExtractedText());
                tableLayout.addView(row);
            }
        });
    }

    private void addHeaderCell(TableRow row, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(getResources().getColor(android.R.color.white));
        row.addView(textView);
    }

    private void addCell(TableRow row, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(textView);
    }

    public abstract class TableSelector implements TabLayout.OnTabSelectedListener {
        @Override
        public abstract void onTabSelected(TabLayout.Tab tab);

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }
} 