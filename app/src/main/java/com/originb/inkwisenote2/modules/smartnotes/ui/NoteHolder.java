package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.graphics.Bitmap;
import android.view.View;
import androidx.activity.ComponentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import lombok.AllArgsConstructor;

import java.util.Optional;

/**
 * Abstract class for note holders used in the smart notebook recycler view.
 * Different types of notes (text, handwritten) will have their own implementations.
 */
public abstract class NoteHolder extends RecyclerView.ViewHolder {
    protected final ComponentActivity parentActivity;
    protected final SmartNotebookRepository smartNotebookRepository;

    public NoteHolder(View itemView, ComponentActivity parentActivity, SmartNotebookRepository smartNotebookRepository) {
        super(itemView);
        this.parentActivity = parentActivity;
        this.smartNotebookRepository = smartNotebookRepository;
    }

    /**
     * Set the note for this holder
     *
     * @param bookId     The ID of the smart notebook
     * @param atomicNote The atomic note entity to display
     */
    public abstract void setNote(long bookId, AtomicNoteEntity atomicNote);

    /**
     * Save the note content
     *
     * @return true if the note was saved successfully
     */
//    public abstract boolean saveNote();

    public abstract NoteHolderData getNoteHolderData();

    @AllArgsConstructor
    public static class NoteHolderData {
        public NoteType noteType;
        public Bitmap bitmap;
        public PageTemplate pageTemplate;
        public String noteText;

        public static NoteHolderData handWrittenNoteData(Bitmap bitmap, PageTemplate pageTemplate) {
            return new NoteHolderData(NoteType.HANDWRITTEN_PNG, bitmap, pageTemplate, "");
        }

        public static NoteHolderData textNoteData(String textData) {
            return new NoteHolderData(NoteType.TEXT_NOTE, null, null, textData);
        }

        public static NoteHolderData initNoteData() {
            return new NoteHolderData(NoteType.NOT_SET, null, null, null);
        }
    }
}
