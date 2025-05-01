package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.Strings;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Fragment for displaying and editing text notes
 */
public class TextNoteFragment extends NoteFragment {

    private EditText noteEditText;
    private ImageButton deleteBtn;
    private ImageButton debugButton;
    private TextNotesDao textNotesDao;
    private TextNoteEntity textNoteEntity;
    private File markdownFile;

    public TextNoteFragment(SmartNotebook smartNotebook, AtomicNoteEntity atomicNote) {
        super(smartNotebook, atomicNote);
        textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.note_text_fragment, container, false);
        noteEditText = itemView.findViewById(R.id.note_edit_text);
        deleteBtn = itemView.findViewById(R.id.delete_note);
        debugButton = itemView.findViewById(R.id.debug_button);

        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View itemView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(itemView, savedInstanceState);
        deleteBtn.setOnClickListener(view ->
                EventBus.getDefault()
                        .post(new Events.DeleteNoteCommand(smartNotebook,
                                atomicNote))
        );

        debugButton.setOnClickListener(v -> {
            showDebugDialog();
        });

        loadNote();
    }

    private void showDebugDialog() {
        if (getContext() != null) {
            NoteDebugDialog dialog = new NoteDebugDialog(getContext(), atomicNote, smartNotebook);
            dialog.show();
        }
    }

    protected void loadNote() {
        BackgroundOps.execute(() -> {
            // Check for markdown file first
            String markdownContent = loadMarkdownFile();
            // Get or create the text note entity for metadata
            textNoteEntity = textNotesDao.getTextNoteForNote(atomicNote.getNoteId());
            if (textNoteEntity == null) {
                textNoteEntity = new TextNoteEntity(atomicNote.getNoteId(), smartNotebook.smartBook.getBookId());
                textNotesDao.insertTextNote(textNoteEntity);
            }
            if (Strings.isNotEmpty(markdownContent) && !markdownContent.equals(textNoteEntity.getNoteText())) {
                textNoteEntity.setNoteText(markdownContent);
                textNotesDao.updateTextNote(textNoteEntity);
            } else if (Strings.isNullOrWhitespace(markdownContent)
                    && Strings.isNotEmpty(textNoteEntity.getNoteText())) {
                markdownContent = textNoteEntity.getNoteText();
            }

            return markdownContent;
        }, noteText -> {
            if (noteEditText != null) {
                noteEditText.setText(noteText);
            }
        });
    }

    /**
     * Load text from markdown file if it exists
     */
    private String loadMarkdownFile() {
        if (getContext() == null || Strings.isNullOrWhitespace(atomicNote.getFilepath())) return null;

        // Create markdown file path using notebook directory structure
        String markdownPath = getMarkdownFilePath();
        markdownFile = new File(markdownPath);

        if (!markdownFile.exists()) {
            return null;
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(markdownFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            if (content.length() > 0) {
                content.deleteCharAt(content.length() - 1);
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error reading note file", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }

    @Override
    public NoteHolderData getNoteHolderData() {
        String text = noteEditText != null ? noteEditText.getText().toString().trim() : "";
        return NoteHolderData.textNoteData(text);
    }

    private String getMarkdownFilePath() {
        return atomicNote.getFilepath() + "/" + atomicNote.getFilename() + ".md";
    }
}
