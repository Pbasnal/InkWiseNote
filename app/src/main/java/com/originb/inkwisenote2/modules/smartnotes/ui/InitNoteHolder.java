package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.view.View;
import androidx.activity.ComponentActivity;
import androidx.cardview.widget.CardView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;

public class InitNoteHolder extends NoteHolder {

    private final Logger logger = new Logger("InitNoteHolder");
    private final CardView cardToHandwriting;
    private final CardView cardToText;
    private final SmartNotebookAdapter adapter;

    private AtomicNoteEntity atomicNote = null;

    public InitNoteHolder(View itemView,
                          ComponentActivity parentActivity,
                          SmartNotebookRepository smartNotebookRepository,
                          SmartNotebookAdapter adapter) {
        super(itemView, parentActivity, smartNotebookRepository);
        this.adapter = adapter;

        cardToHandwriting = itemView.findViewById(R.id.touch_to_write);
        cardToHandwriting.setOnClickListener(this::createHandwrittenNote);

        cardToText = itemView.findViewById(R.id.tap_to_text);
        cardToText.setOnClickListener(this::createTextNote);
    }

    private void createTextNote(View view) {
        if (atomicNote == null) return;
        adapter.updateNoteType(atomicNote, NoteType.TEXT_NOTE.toString());
    }

    private void createHandwrittenNote(View view) {
        if (atomicNote == null) return;
        adapter.updateNoteType(atomicNote, NoteType.HANDWRITTEN_PNG.toString());
    }

    @Override
    public void setNote(long bookId, AtomicNoteEntity atomicNote) {
        this.atomicNote = atomicNote;
        logger.debug("Setting note");
    }

    @Override
    public boolean saveNote() {
        return true;
    }
}
