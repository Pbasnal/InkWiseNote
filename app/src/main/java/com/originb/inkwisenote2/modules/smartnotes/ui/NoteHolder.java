package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.view.View;
import androidx.activity.ComponentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;

public abstract class NoteHolder extends RecyclerView.ViewHolder {
    protected final ComponentActivity parentActivity;
    protected final SmartNotebookRepository smartNotebookRepository;

    public NoteHolder(View itemView, ComponentActivity parentActivity, SmartNotebookRepository smartNotebookRepository) {
        super(itemView);
        this.parentActivity = parentActivity;
        this.smartNotebookRepository = smartNotebookRepository;
    }

    public abstract void setNote(long bookId, AtomicNoteEntity atomicNote);

    public abstract boolean saveNote();
}
