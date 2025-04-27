package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.DateTimeUtils;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;

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

    private final SmartNotebookRepository smartNotebookRepository;
    private final TextNotesDao textNotesDao;

    public NoteDebugDialog(@NonNull Context context, AtomicNoteEntity atomicNote, SmartNotebook currentSmartNotebook) {
        super(context);
        this.atomicNote = atomicNote;
        this.currentSmartNotebook = currentSmartNotebook;

        // Get repositories
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        this.textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_note_debug_info);

        // Initialize views
        noteInfoTable = findViewById(R.id.note_info_table);
        relatedNotesTable = findViewById(R.id.related_notes_table);
        smartbooksTable = findViewById(R.id.smartbooks_table);
        parsedTextContent = findViewById(R.id.parsed_text_content);

        Button closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dismiss());

        // Set dialog title
        setTitle("Note Debug Information");

        // Load debug data
        loadDebugData();
    }

    private void loadDebugData() {
        // Load basic note info
        addBasicNoteInfo();

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

        // Get parsed text if it's a text note
        TextNoteEntity textNote = textNotesDao.getTextNoteForNote(atomicNote.getNoteId());
        if (textNote != null) {
            data.parsedText = textNote.getNoteText();
        }

        return data;
    }

    private void updateDebugUI(DebugData data) {
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
    }

    private void addBasicNoteInfo() {
        addRowToTable(noteInfoTable, "Note ID", String.valueOf(atomicNote.getNoteId()));
        addRowToTable(noteInfoTable, "Note Type", atomicNote.getNoteType());
        addRowToTable(noteInfoTable, "Created", DateTimeUtils.msToDateTime(atomicNote.getCreatedTimeMillis()));
        addRowToTable(noteInfoTable, "Last Modified", DateTimeUtils.msToDateTime(atomicNote.getLastModifiedTimeMillis()));
        addRowToTable(noteInfoTable, "Working Note Path", atomicNote.getFilepath() + "/" + atomicNote.getFilename());
    }

    private void addRowToTable(TableLayout table, String key, String value) {
        TableRow row = new TableRow(getContext());

        // Create key TextView
        TextView keyView = new TextView(getContext());
        keyView.setText(key);
        keyView.setPadding(8, 8, 16, 8);
        keyView.setTextColor(getContext().getResources().getColor(android.R.color.black));

        // Create value TextView
        TextView valueView = new TextView(getContext());
        valueView.setText(value);
        valueView.setPadding(16, 8, 8, 8);

        // Add views to row
        row.addView(keyView);
        row.addView(valueView);

        // Add row to table
        table.addView(row);
    }

    private void addSeparatorToTable(TableLayout table) {
        TableRow row = new TableRow(getContext());
        View separator = new View(getContext());
        separator.setBackgroundColor(getContext().getResources().getColor(android.R.color.darker_gray));
        separator.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1, 2));
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
    }
} 