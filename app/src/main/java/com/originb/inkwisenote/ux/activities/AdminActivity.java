package com.originb.inkwisenote.ux.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.data.admin.NoteExtractedTextEntry;
import com.originb.inkwisenote.data.admin.TermFrequencyEntry;
import com.originb.inkwisenote.io.sql.NoteTermFrequencyContract;
import com.originb.inkwisenote.io.sql.NoteTextContract;
import com.originb.inkwisenote.modules.repositories.Repositories;

import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private NoteTermFrequencyContract.NoteTermFrequencyDbQueries termFrequencyQueries;
    private NoteTextContract.NoteTextDbHelper noteTextDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        tableLayout = findViewById(R.id.data_table);
        termFrequencyQueries = Repositories.getInstance().getNoteTermFrequencyDbQueries();
        noteTextDbHelper = Repositories.getInstance().getNoteTextDbHelper();

        TabLayout tabLayout = findViewById(R.id.table_selector_tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showTermFrequencyData();
                } else {
                    showNoteTextData();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Show term frequency data by default
        showTermFrequencyData();
    }

    private void showTermFrequencyData() {
        tableLayout.removeAllViews();
        
        // Add header
        TableRow headerRow = new TableRow(this);
        addHeaderCell(headerRow, "Note ID");
        addHeaderCell(headerRow, "Term");
        addHeaderCell(headerRow, "Frequency");
        tableLayout.addView(headerRow);

        // Add data rows
        List<TermFrequencyEntry> entries = termFrequencyQueries.getAllTermFrequencies();
        for (TermFrequencyEntry entry : entries) {
            TableRow row = new TableRow(this);
            addCell(row, String.valueOf(entry.getNoteId()));
            addCell(row, entry.getTerm());
            addCell(row, String.valueOf(entry.getFrequency()));
            tableLayout.addView(row);
        }
    }

    private void showNoteTextData() {
        tableLayout.removeAllViews();
        
        // Add header
        TableRow headerRow = new TableRow(this);
        addHeaderCell(headerRow, "Note ID");
        addHeaderCell(headerRow, "Text");
        tableLayout.addView(headerRow);

        // Add data rows
        List<NoteExtractedTextEntry> entries = noteTextDbHelper.getAllNoteText();
        for (NoteExtractedTextEntry entry : entries) {
            TableRow row = new TableRow(this);
            addCell(row, String.valueOf(entry.getNoteId()));
            addCell(row, entry.getText());
            tableLayout.addView(row);
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        termFrequencyQueries.close();
        noteTextDbHelper.close();
    }
} 