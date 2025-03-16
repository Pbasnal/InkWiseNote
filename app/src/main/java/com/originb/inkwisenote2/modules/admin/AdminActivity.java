package com.originb.inkwisenote2.modules.admin;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
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