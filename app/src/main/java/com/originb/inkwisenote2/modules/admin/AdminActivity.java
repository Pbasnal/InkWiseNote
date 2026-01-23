package com.originb.inkwisenote2.modules.admin;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.tabs.TabLayout;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteEntity;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText;
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPage;
import java.io.File;
import android.widget.*;
import org.koin.android.compat.ViewModelCompat;

public class AdminActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private EditText editText;
    private Button filterQueryBtn;
    private String selectedTab = "Term Frequencies";
    private AdminViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // UI Initialization
        editText = findViewById(R.id.note_id_query);
        filterQueryBtn = findViewById(R.id.filter_query);
        tableLayout = findViewById(R.id.data_table);
        TabLayout tabLayout = findViewById(R.id.table_selector_tabs);

        // ViewModel Initialization
        viewModel = ViewModelCompat.getViewModel(this, AdminViewModel.class);

        // Observers
        observeViewModel();

        // Listeners
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getText().toString();
                triggerRefresh();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        filterQueryBtn.setOnClickListener(v -> triggerRefresh());

//         Initial Load
        triggerRefresh();
    }

    private void observeViewModel() {
        viewModel.uiState.observe(this, state -> {
            if (state instanceof AdminUiState.DataList) {
                renderDbTable((AdminUiState.DataList) state);
            } else if (state instanceof AdminUiState.FilesState) {
                renderFilesTable((AdminUiState.FilesState) state);
            }
        });

        viewModel.toastMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void triggerRefresh() {
        String input = editText.getText().toString();
        Long noteId = input.isEmpty() ? 0L : Long.parseLong(input);
        viewModel.loadData(selectedTab, noteId, getFilesDir());
    }

    // --- Rendering Logic ---

    private void renderDbTable(AdminUiState.DataList state) {
        tableLayout.removeAllViews();

        if (state.data == null || state.data.isEmpty()) {
            renderEmptyState("No data available for this category");
            return;
        }

        TableRow headerRow = new TableRow(this);

        // Column Header Logic
        switch (state.type) {
            case "Term Frequencies":
                addHeaderCell(headerRow, "Note ID", "Term", "Frequency");
                break;
            case "Note Text":
                addHeaderCell(headerRow, "Note ID", "Text");
                break;
            case "Atomic Notes":
                addHeaderCell(headerRow, "Note ID", "Filename", "Filepath", "Type");
                break;
            case "Smart Books":
                addHeaderCell(headerRow, "Book ID", "Title");
                break;
            case "Smart Book Pages":
                addHeaderCell(headerRow, "Book ID", "Note ID");
                break;
            case "Handwritten Notes":
                addHeaderCell(headerRow, "Note ID", "Book ID", "Bitmap", "PageT");
                break;
        }
        tableLayout.addView(headerRow);

        // Data Row Logic
        for (Object item : state.data) {
            TableRow row = new TableRow(this);
            if (item instanceof NoteTermFrequency) {
                NoteTermFrequency e = (NoteTermFrequency) item;
                addCells(row, String.valueOf(e.getNoteId()), e.getTerm(), String.valueOf(e.getTermFrequency()));
            } else if (item instanceof NoteOcrText) {
                NoteOcrText e = (NoteOcrText) item;
                addCells(row, String.valueOf(e.getNoteId()), e.getExtractedText());
            } else if (item instanceof AtomicNoteEntity) {
                AtomicNoteEntity e = (AtomicNoteEntity) item;
                addCells(row, String.valueOf(e.getNoteId()), e.getFilename(), e.getFilepath(), e.getNoteType());
            } else if (item instanceof SmartBookEntity) {
                SmartBookEntity e = (SmartBookEntity) item;
                addCells(row, String.valueOf(e.getBookId()), e.getTitle());
            } else if (item instanceof SmartBookPage) {
                SmartBookPage e = (SmartBookPage) item;
                addCells(row, String.valueOf(e.getBookId()), String.valueOf(e.getNoteId()));
            } else if (item instanceof HandwrittenNoteEntity) {
                HandwrittenNoteEntity e = (HandwrittenNoteEntity) item;
                addCells(row, String.valueOf(e.getNoteId()), String.valueOf(e.getBookId()), e.getBitmapFilePath(), e.getPageTemplateFilePath());
            }
            tableLayout.addView(row);
        }
    }

    private void renderEmptyState(String message) {
        TableRow row = new TableRow(this);
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setPadding(20, 20, 20, 20);
        row.addView(tv);
        tableLayout.addView(row);
    }

    private void renderFilesTable(AdminUiState.FilesState state) {
        tableLayout.removeAllViews();

        // Navigation Row
        TableRow navRow = new TableRow(this);
        TextView pathView = new TextView(this);
        pathView.setText("Path: " + state.currentDir.getAbsolutePath());
        pathView.setPadding(16, 16, 16, 16);
        navRow.addView(pathView);

        Button upBtn = new Button(this);
        upBtn.setText("↑ Up");
        upBtn.setOnClickListener(v -> viewModel.navigateToDir(state.currentDir.getParentFile()));
        navRow.addView(upBtn);
        tableLayout.addView(navRow);

        // Header
        TableRow headerRow = new TableRow(this);
        addHeaderCell(headerRow, "Name", "Size", "Type", "Action");
        tableLayout.addView(headerRow);

        // File Rows
        if (state.files != null) {
            for (File file : state.files) {
                TableRow row = new TableRow(this);
                addCells(row, file.getName(), String.valueOf(file.length()), file.isDirectory() ? "DIR" : "FILE");

                LinearLayout actions = new LinearLayout(this);
                if (file.isDirectory()) {
                    Button open = new Button(this);
                    open.setText("Open");
                    open.setOnClickListener(v -> viewModel.navigateToDir(file));
                    actions.addView(open);
                }
                Button del = new Button(this);
                del.setText("Del");
                del.setOnClickListener(v -> viewModel.deleteFile(file));
                actions.addView(del);

                row.addView(actions);
                tableLayout.addView(row);
            }
        }
    }

    // --- Helpers ---

    private void addHeaderCell(TableRow row, String... titles) {
        for (String title : titles) {
            TextView tv = new TextView(this);
            tv.setText(title);
            tv.setPadding(16, 16, 16, 16);
            tv.setTypeface(null, Typeface.BOLD);
            row.addView(tv);
        }
    }

    private void addCells(TableRow row, String... contents) {
        for (String content : contents) {
            TextView tv = new TextView(this);
            tv.setText(content);
            tv.setPadding(16, 16, 16, 16);
            row.addView(tv);
        }
    }
}