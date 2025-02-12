package com.originb.inkwisenote.ux.activities.smartnotebook;

import android.graphics.Bitmap;
import android.view.View;
import androidx.activity.ComponentActivity;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.data.notedata.PageTemplate;
import com.originb.inkwisenote.modules.repositories.HandwrittenNoteRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote.ux.views.DrawingView;
import com.originb.inkwisenote.ux.views.PageBackgroundType;

import java.util.Optional;

public class HandwrittenNoteHolder extends NoteHolder {

    private final DrawingView drawingView;
    private AtomicNoteEntity atomicNote;

    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private ConfigReader configReader;

    public HandwrittenNoteHolder(View itemView, ComponentActivity parentActivity, SmartNotebookRepository smartNotebookRepository) {
        super(itemView, parentActivity, smartNotebookRepository);
        drawingView = itemView.findViewById(R.id.smart_drawing_view);

        handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
        configReader = ConfigReader.getInstance();
    }

    @Override
    public void setNote(AtomicNoteEntity atomicNote) {
        this.atomicNote = atomicNote;
        Optional<Bitmap> bitmapOpt = handwrittenNoteRepository.getNoteImage(atomicNote, true);
        Optional<PageTemplate> pageTemplateOpt = handwrittenNoteRepository.getPageTemplate(atomicNote);
        if (pageTemplateOpt.isPresent()) {
            pageTemplateOpt.ifPresent(drawingView::setPageTemplate);
        } else {
            PageTemplate pageTemplate = configReader.getAppConfig().getPageTemplates().get(PageBackgroundType.BASIC_RULED_PAGE_TEMPLATE.name());
            handwrittenNoteRepository.saveHandwrittenNotePageTemplate(atomicNote, pageTemplate);

            drawingView.setPageTemplate(pageTemplate);
        }
        if (bitmapOpt.isPresent()) {
            bitmapOpt.ifPresent(drawingView::setBitmap);
        } else {
            Bitmap newBitmap = Bitmap.createBitmap(drawingView.currentWidth, drawingView.currentHeight, Bitmap.Config.ARGB_8888);
            handwrittenNoteRepository.saveHandwrittenNoteImage(atomicNote, newBitmap);
            drawingView.setBitmap(newBitmap);
        }
    }

    @Override
    public void saveNote() {
        handwrittenNoteRepository.saveHandwrittenNotes(atomicNote, drawingView.getBitmap(), drawingView.getPageTemplate());
    }
}
