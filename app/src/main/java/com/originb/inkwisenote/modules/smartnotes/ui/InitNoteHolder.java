package com.originb.inkwisenote.modules.smartnotes.ui;

import android.view.View;
import androidx.activity.ComponentActivity;
import androidx.cardview.widget.CardView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote.modules.smartnotes.data.AtomicNoteEntity;

public class InitNoteHolder extends NoteHolder {

    private final CardView cardToHandwriting;

    public InitNoteHolder(View itemView, ComponentActivity parentActivity, SmartNotebookRepository smartNotebookRepository) {
        super(itemView, parentActivity, smartNotebookRepository);
        cardToHandwriting = itemView.findViewById(R.id.touch_to_write);

        cardToHandwriting.setOnClickListener(this::createHandwrittenNote);

    }

    private void createHandwrittenNote(View view) {

    }

    @Override
    public void setNote(long bookId, AtomicNoteEntity atomicNote) {

    }

    @Override
    public boolean saveNote() {
        return false;
    }
}
