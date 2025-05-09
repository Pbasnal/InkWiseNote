package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.DateTimeUtils;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText;
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextDao;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog for displaying debug information about a note
 */
public class NoteDebugDialog extends Dialog {

    private final AtomicNoteEntity atomicNote;
    private final SmartNotebook currentSmartNotebook;

    private TableLayout noteInfoTable;
    private TableLayout relatedNotesTable;
    private TableLayout smartbooksTable;
    private TextView parsedTextContent;
    private TextView markdownStrokesContent;

    private final SmartNotebookRepository smartNotebookRepository;
    private final TextNotesDao textNotesDao;
    private final NoteOcrTextDao noteOcrTextDao;
    private final HandwrittenNoteRepository handwrittenNoteRepository;

    public NoteDebugDialog(@NonNull Context context, AtomicNoteEntity atomicNote, SmartNotebook currentSmartNotebook) {
        super(context);
        this.atomicNote = atomicNote;
        this.currentSmartNotebook = currentSmartNotebook;

        // Get repositories
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        this.textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();
        this.noteOcrTextDao = Repositories.getInstance().getNotesDb().noteOcrTextDao();
        this.handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_note_debug_info);

        // Set dialog to use full width and almost full height
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            getWindow().setGravity(Gravity.CENTER);
        }

        // Initialize tables
        noteInfoTable = findViewById(R.id.tableNoteInfo);
        relatedNotesTable = findViewById(R.id.relatedNotesTable);
        smartbooksTable = findViewById(R.id.smartbooksTable);
        parsedTextContent = findViewById(R.id.parsedTextContent);
        markdownStrokesContent = findViewById(R.id.markdownStrokesContent);

        // Set close button click listener
        Button closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dismiss());

        // Load note info if available
        if (atomicNote != null) {
            loadNoteInfo();
        }
    }

    private void loadNoteInfo() {
        // Load basic note info
        addBasicNoteInfo();

        // Add loading indicator to tables before background loading
        addRowToTable(relatedNotesTable, "Loading related notes...", "");
        addRowToTable(smartbooksTable, "Loading smartbooks...", "");
        parsedTextContent.setText("Loading parsed text...");
        markdownStrokesContent.setText("Loading markdown strokes data...");

        // Load related notes and smartbooks in background
        BackgroundOps.execute(this::collectDebugData, this::updateDebugUI);
    }

    private DebugData collectDebugData() {
        DebugData data = new DebugData();

        // Get related notes (notes in the same smartbook)
        if (currentSmartNotebook != null) {
            data.relatedNotes = new ArrayList<>(currentSmartNotebook.getAtomicNotes());
            // Remove the current note
            data.relatedNotes.remove(atomicNote);
        }

        // Get all smartbooks containing this note
        data.smartbooks = smartNotebookRepository.getSmartNotebookContainingNote(atomicNote.getNoteId())
                .stream().map(SmartNotebook::getSmartBook)
                .collect(Collectors.toList());

        NoteType noteType = NoteType.fromString(atomicNote.getNoteType());
        switch (noteType) {
            case TEXT_NOTE:
                // Get parsed text if it's a text note
                TextNoteEntity textNote = textNotesDao.getTextNoteForNote(atomicNote.getNoteId());
                if (textNote != null) {
                    data.parsedText = textNote.getNoteText();
                }
                break;
            case HANDWRITTEN_PNG:
                // Get parsed text if it's a text note
                NoteOcrText noteOcrText = noteOcrTextDao.readTextFromDb(atomicNote.getNoteId());
                if (noteOcrText != null) {
                    data.parsedText = noteOcrText.getExtractedText();
                }
                
                // Get markdown strokes content
                data.markdownContent = readMarkdownFile(atomicNote);
                break;
            default:
                data.parsedText = "Note doesn't have text";
                break;
        }

        return data;
    }

    /**
     * Reads the markdown file containing strokes data
     * @param note The note entity to read markdown for
     * @return The markdown file content or a message if not found
     */
    private String readMarkdownFile(AtomicNoteEntity note) {
        String markdownPath = note.getFilepath() + "/" + note.getFilename() + ".md";
        File file = new File(markdownPath);
        
        if (!file.exists() || !file.isFile()) {
            return "No markdown file found at: " + markdownPath;
        }
        
        try {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            return content.toString();
        } catch (IOException e) {
            return "Error reading markdown file: " + e.getMessage();
        }
    }

    private void updateDebugUI(DebugData data) {
        // Clear existing tables
        relatedNotesTable.removeAllViews();
        smartbooksTable.removeAllViews();

        // Update related notes table
        if (data.relatedNotes != null && !data.relatedNotes.isEmpty()) {
            for (AtomicNoteEntity relatedNote : data.relatedNotes) {
                addRowToTable(relatedNotesTable, "Note ID", String.valueOf(relatedNote.getNoteId()));
                addRowToTable(relatedNotesTable, "Note Type", relatedNote.getNoteType());
                addRowToTable(relatedNotesTable, "Created", DateTimeUtils.msToDateTime(relatedNote.getCreatedTimeMillis()));
                addRowToTable(relatedNotesTable, "Modified", DateTimeUtils.msToDateTime(relatedNote.getLastModifiedTimeMillis()));
                addSeparatorToTable(relatedNotesTable);
            }
        } else {
            addRowToTable(relatedNotesTable, "No related notes found", "");
        }

        // Update smartbooks table
        if (data.smartbooks != null && !data.smartbooks.isEmpty()) {
            for (SmartBookEntity smartbook : data.smartbooks) {
                addRowToTable(smartbooksTable, "Book ID", String.valueOf(smartbook.getBookId()));
                addRowToTable(smartbooksTable, "Title", smartbook.getTitle());
                addRowToTable(smartbooksTable, "Created", DateTimeUtils.msToDateTime(smartbook.getCreatedTimeMillis()));
                addRowToTable(smartbooksTable, "Modified", DateTimeUtils.msToDateTime(smartbook.getLastModifiedTimeMillis()));
                addSeparatorToTable(smartbooksTable);
            }
        } else {
            addRowToTable(smartbooksTable, "No smartbooks found", "");
        }

        // Update parsed text
        if (data.parsedText != null && !data.parsedText.isEmpty()) {
            parsedTextContent.setText(data.parsedText);
        } else {
            parsedTextContent.setText("No parsed text available");
        }
        
        // Update markdown strokes content
        if (data.markdownContent != null && !data.markdownContent.isEmpty()) {
            markdownStrokesContent.setText(data.markdownContent);
        } else {
            markdownStrokesContent.setText("No markdown strokes data available");
        }
    }

    private void addBasicNoteInfo() {
        addRowToTable(noteInfoTable, "Note ID", String.valueOf(atomicNote.getNoteId()));
        addRowToTable(noteInfoTable, "Note Type", atomicNote.getNoteType());
        addRowToTable(noteInfoTable, "Created", DateTimeUtils.msToDateTime(atomicNote.getCreatedTimeMillis()));
        addRowToTable(noteInfoTable, "Last Modified", DateTimeUtils.msToDateTime(atomicNote.getLastModifiedTimeMillis()));
        addRowToTable(noteInfoTable, "Working Note Path", atomicNote.getFilepath() + "/" + atomicNote.getFilename());
        
        // Add markdown file path if it's a handwritten note
        if (NoteType.HANDWRITTEN_PNG.name().equals(atomicNote.getNoteType())) {
            String markdownPath = atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".md";
            File markdownFile = new File(markdownPath);
            String markdownStatus = markdownFile.exists() ? "Exists" : "Not created yet";
            addRowToTable(noteInfoTable, "Markdown File", markdownPath + " (" + markdownStatus + ")");
        }
    }

    private void addRowToTable(TableLayout table, String key, String value) {
        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        // Create key TextView
        TextView keyView = new TextView(getContext());
        keyView.setText(key);
        keyView.setPadding(8, 8, 16, 8);
        keyView.setTextColor(getContext().getResources().getColor(android.R.color.black));
        // Set layout params with weight
        TableRow.LayoutParams keyParams = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 0.3f);
        keyView.setLayoutParams(keyParams);
        keyView.setGravity(Gravity.START | Gravity.TOP);

        // Create value TextView
        TextView valueView = new TextView(getContext());
        valueView.setText(value);
        valueView.setPadding(16, 8, 8, 8);
        // Set layout params with weight
        TableRow.LayoutParams valueParams = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 0.7f);
        valueView.setLayoutParams(valueParams);
        valueView.setGravity(Gravity.START | Gravity.TOP);
        valueView.setSingleLine(false);

        // Add views to row
        row.addView(keyView);
        row.addView(valueView);

        // Add row to table
        table.addView(row);
    }

    private void addSeparatorToTable(TableLayout table) {
        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        View separator = new View(getContext());
        separator.setBackgroundColor(getContext().getResources().getColor(android.R.color.darker_gray));

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, 1);
        params.span = 2;
        params.topMargin = 4;
        params.bottomMargin = 4;
        separator.setLayoutParams(params);

        row.addView(separator);
        table.addView(row);
    }

    /**
     * Class to hold debug data collected in the background
     */
    private static class DebugData {
        List<AtomicNoteEntity> relatedNotes;
        List<SmartBookEntity> smartbooks;
        String parsedText;
        String markdownContent;
    }
} 