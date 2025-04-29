package com.originb.inkwisenote2.modules.admin;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.tabs.TabLayout;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNotesDao;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntitiesDao;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPagesDao;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBooksDao;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteEntity;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText;
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPage;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AdminActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private NoteTermFrequencyDao noteTermFrequencyDao;
    private NoteOcrTextDao noteOcrTextDao;
    private AtomicNoteEntitiesDao atomicNoteEntitiesDao;
    private SmartBooksDao smartBooksDao;
    private SmartBookPagesDao smartBookPagesDao;
    private HandwrittenNotesDao handwrittenNotesDao;

    private EditText editText;
    private Button filterQueryBtn;
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
        atomicNoteEntitiesDao = Repositories.getInstance().getNotesDb().atomicNoteEntitiesDao();
        smartBooksDao = Repositories.getInstance().getNotesDb().smartBooksDao();
        smartBookPagesDao = Repositories.getInstance().getNotesDb().smartBookPagesDao();
        handwrittenNotesDao = Repositories.getInstance().getNotesDb().handwrittenNotesDao();

        tablePopulators.put("Term Frequencies", this::showTermFrequencyData);
        tablePopulators.put("Note Text", this::showNoteTextData);
        tablePopulators.put("Atomic Notes", this::showNoteAtomicNotes);
        tablePopulators.put("Smart Books", this::showSmartBooks);
        tablePopulators.put("Smart Book Pages", this::showSmartBookPages);
        tablePopulators.put("Handwritten Notes", this::showHandWrittenNotes);
        tablePopulators.put("Files", this::showFilesData);

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

    /**
     * Shows files in the app's files directory with delete option
     */
    private void showFilesData(Long noteId) {
        tableLayout.removeAllViews();

        // Add header and navigation row
        TableRow navigationRow = new TableRow(this);
        
        // Create current path text display
        TextView currentPathView = new TextView(this);
        currentPathView.setPadding(16, 16, 16, 16);
        currentPathView.setTypeface(null, Typeface.BOLD);
        
        // Default to app's files directory
        File currentDir = getFilesDir();
        currentPathView.setText("Current Path: " + currentDir.getAbsolutePath());
        
        // Create a layout for buttons
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        // Add parent directory button
        Button upButton = new Button(this);
        upButton.setText("↑ Up");
        upButton.setOnClickListener(v -> {
            File parentDir = currentDir.getParentFile();
            if (parentDir != null && parentDir.canRead()) {
                displayFilesInDirectory(parentDir);
                currentPathView.setText("Current Path: " + parentDir.getAbsolutePath());
            } else {
                Toast.makeText(this, "Cannot access parent directory", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Add refresh button
        Button refreshButton = new Button(this);
        refreshButton.setText("🔄 Refresh");
        refreshButton.setOnClickListener(v -> {
            displayFilesInDirectory(currentDir);
            currentPathView.setText("Current Path: " + currentDir.getAbsolutePath());
        });
        
        // Add buttons to layout
        buttonLayout.addView(upButton);
        buttonLayout.addView(refreshButton);
        
        // Add views to navigation row with proper layout
        TableRow.LayoutParams pathParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.7f);
        navigationRow.addView(currentPathView, pathParams);
        
        TableRow.LayoutParams buttonParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.3f);
        navigationRow.addView(buttonLayout, buttonParams);
        
        tableLayout.addView(navigationRow);
        
        // Add headers row
        TableRow headerRow = new TableRow(this);
        addHeaderCell(headerRow, "Filename");
        addHeaderCell(headerRow, "Path");
        addHeaderCell(headerRow, "Size (Bytes)");
        addHeaderCell(headerRow, "Type");
        addHeaderCell(headerRow, "Actions");
        tableLayout.addView(headerRow);

        // Display files in the directory
        displayFilesInDirectory(currentDir);
    }

    /**
     * Display files from a specific directory
     */
    private void displayFilesInDirectory(File directory) {
        // Remove existing file rows but keep the header
        for (int i = tableLayout.getChildCount() - 1; i >= 2; i--) {
            tableLayout.removeViewAt(i);
        }
        
        File[] files = directory.listFiles();
        
        // Sort files by name
        if (files != null && files.length > 0) {
            // Sort directories first, then files alphabetically
            Arrays.sort(files, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            });

            for (File file : files) {
                addFileRow(file, directory);
            }
        } else {
            TableRow row = new TableRow(this);
            TextView emptyText = new TextView(this);
            emptyText.setText("No files found in this directory");
            emptyText.setPadding(16, 16, 16, 16);
            emptyText.setGravity(Gravity.CENTER);
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.span = 5; // Span across all columns
            emptyText.setLayoutParams(params);
            row.addView(emptyText);
            tableLayout.addView(row);
        }
    }

    /**
     * Adds a row to the table for a file with delete button
     */
    private void addFileRow(File file, File currentDir) {
        TableRow row = new TableRow(this);

        // Add file information
        addCell(row, file.getName());
        addCell(row, file.getAbsolutePath());
        addCell(row, String.valueOf(file.length()));
        
        // Determine file type
        String fileType = "File";
        if (file.isDirectory()) {
            fileType = "Directory";
        } else {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".png")) {
                fileType = "Image (PNG)";
            } else if (name.endsWith(".md")) {
                fileType = "Markdown";
            } else if (name.endsWith(".pt")) {
                fileType = "Page Template";
            } else if (name.endsWith(".db")) {
                fileType = "Database";
            }
        }
        addCell(row, fileType);

        // Add action buttons
        LinearLayout actionLayout = new LinearLayout(this);
        actionLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        // If it's a directory, add an Open button
        if (file.isDirectory()) {
            Button openButton = new Button(this);
            openButton.setText("Open");
            openButton.setOnClickListener(v -> {
                displayFilesInDirectory(file);
                
                // Update the current path in the UI
                for (int i = 0; i < tableLayout.getChildCount(); i++) {
                    View view = tableLayout.getChildAt(i);
                    if (view instanceof TableRow && i == 0) {
                        TableRow navRow = (TableRow) view;
                        if (navRow.getChildCount() > 0 && navRow.getChildAt(0) instanceof TextView) {
                            TextView pathView = (TextView) navRow.getChildAt(0);
                            pathView.setText("Current Path: " + file.getAbsolutePath());
                        }
                        break;
                    }
                }
            });
            actionLayout.addView(openButton);
        }
        
        // Add Delete button for all files and directories
        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setOnClickListener(v -> {
            if (file.isDirectory()) {
                // Check if directory is empty
                File[] contents = file.listFiles();
                if (contents != null && contents.length > 0) {
                    Toast.makeText(this, "Cannot delete non-empty directory", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            if (file.delete()) {
                tableLayout.removeView(row);
                Toast.makeText(this, "Deleted: " + file.getName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete: " + file.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        
        actionLayout.addView(deleteButton);
        row.addView(actionLayout);
        tableLayout.addView(row);
    }

    private void showHandWrittenNotes(Long noteId) {
        tableLayout.removeAllViews();

        // Add header
        TableRow headerRow = new TableRow(this);
        addHeaderCell(headerRow, "Note ID");
        addHeaderCell(headerRow, "Book ID");
        addHeaderCell(headerRow, "Bitmap");
        addHeaderCell(headerRow, "PageT");
        tableLayout.addView(headerRow);

        // Add data rows
        BackgroundOps.execute(() -> handwrittenNotesDao.getAllHandwrittenNotes(), entries -> {
            for (HandwrittenNoteEntity entry : entries) {
                TableRow row = new TableRow(this);
                addCell(row, String.valueOf(entry.getNoteId()));
                addCell(row, String.valueOf(entry.getBookId()));
                addCell(row, entry.getBitmapFilePath());
                addCell(row, entry.getPageTemplateFilePath());
                tableLayout.addView(row);
            }
        });
    }

    private void showNoteAtomicNotes(Long noteId) {
        tableLayout.removeAllViews();

        // Add header
        TableRow headerRow = new TableRow(this);
        addHeaderCell(headerRow, "Note ID");
        addHeaderCell(headerRow, "filename");
        addHeaderCell(headerRow, "filepath");
        addHeaderCell(headerRow, "note_type");
        tableLayout.addView(headerRow);

        // Add data rows
        BackgroundOps.execute(() -> atomicNoteEntitiesDao.getAllAtomicNotes(), entries -> {

            if (CollectionUtils.isEmpty(entries)) return;

            for (AtomicNoteEntity entry : entries) {
                TableRow row = new TableRow(this);
                addCell(row, String.valueOf(entry.getNoteId()));
                addCell(row, entry.getFilename());
                addCell(row, entry.getFilepath());
                addCell(row, entry.getNoteType());
                tableLayout.addView(row);
            }
        });
    }

    private void showSmartBooks(Long noteId) {
        tableLayout.removeAllViews();

        // Add header
        TableRow headerRow = new TableRow(this);
        addHeaderCell(headerRow, "Book ID");
        addHeaderCell(headerRow, "Title");
        tableLayout.addView(headerRow);

        // Add data rows
        BackgroundOps.execute(() -> smartBooksDao.getAllSmartBooks(), entries -> {

            if (CollectionUtils.isEmpty(entries)) return;

            for (SmartBookEntity entry : entries) {
                TableRow row = new TableRow(this);
                addCell(row, String.valueOf(entry.getBookId()));
                addCell(row, entry.getTitle());
                tableLayout.addView(row);
            }
        });
    }

    private void showSmartBookPages(Long noteId) {
        tableLayout.removeAllViews();

        // Add header
        TableRow headerRow = new TableRow(this);
        addHeaderCell(headerRow, "Book ID");
        addHeaderCell(headerRow, "Note ID");
        tableLayout.addView(headerRow);

        // Add data rows
        BackgroundOps.execute(() -> smartBookPagesDao.getAllSmartBookPages(), entries -> {

            if (CollectionUtils.isEmpty(entries)) return;

            for (SmartBookPage entry : entries) {
                TableRow row = new TableRow(this);
                addCell(row, String.valueOf(entry.getBookId()));
                addCell(row, String.valueOf(entry.getNoteId()));
                tableLayout.addView(row);
            }
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