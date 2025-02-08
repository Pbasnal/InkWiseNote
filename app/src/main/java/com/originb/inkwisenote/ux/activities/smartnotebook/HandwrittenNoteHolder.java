package com.originb.inkwisenote.ux.activities.smartnotebook;

import android.view.View;
import androidx.activity.ComponentActivity;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote.ux.views.DrawingView;

public class HandwrittenNoteHolder extends NoteHolder {

    private final DrawingView drawingView;

    public HandwrittenNoteHolder(View itemView, ComponentActivity parentActivity, SmartNotebookRepository smartNotebookRepository) {
        super(itemView, parentActivity, smartNotebookRepository);
        drawingView = itemView.findViewById(R.id.smart_drawing_view);
    }

    @Override
    public void setNote(AtomicNoteEntity atomicNote) {
        smartNotebookRepository.getNoteImage(atomicNote, true)
                .ifPresent(drawingView::setBitmap);
    }
}
