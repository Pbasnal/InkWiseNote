package com.originb.inkwisenote.ux.activities.smartnotebook;

import android.view.View;
import androidx.activity.ComponentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;

public abstract class NoteHolder extends RecyclerView.ViewHolder {
    protected final ComponentActivity parentActivity;
    protected final SmartNotebookRepository smartNotebookRepository;

    public NoteHolder(View itemView, ComponentActivity parentActivity, SmartNotebookRepository smartNotebookRepository) {
        super(itemView);
        this.parentActivity = parentActivity;
        this.smartNotebookRepository = smartNotebookRepository;
    }

    public abstract void setNote(long bookId, AtomicNoteEntity atomicNote);

    public abstract void saveNote();
}
