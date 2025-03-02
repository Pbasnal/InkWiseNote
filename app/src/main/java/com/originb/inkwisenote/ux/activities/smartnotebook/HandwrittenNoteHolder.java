package com.originb.inkwisenote.ux.activities.smartnotebook;

import android.graphics.Bitmap;
import android.view.View;
import androidx.activity.ComponentActivity;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.data.notedata.PageTemplate;
import com.originb.inkwisenote.modules.messaging.BackgroundOps;
import com.originb.inkwisenote.modules.repositories.HandwrittenNoteRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote.ux.views.DrawingView;
import com.originb.inkwisenote.ux.views.PageBackgroundType;

import java.util.Optional;

public class HandwrittenNoteHolder extends NoteHolder {

    private final DrawingView drawingView;
    private AtomicNoteEntity atomicNote;

    private long bookId;

    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private ConfigReader configReader;

    public HandwrittenNoteHolder(View itemView, ComponentActivity parentActivity, SmartNotebookRepository smartNotebookRepository) {
        super(itemView, parentActivity, smartNotebookRepository);
        drawingView = itemView.findViewById(R.id.smart_drawing_view);

        handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
        configReader = ConfigReader.getInstance();
    }

    @Override
    public void setNote(long bookId, AtomicNoteEntity atomicNote) {
        this.bookId = bookId;
        this.atomicNote = atomicNote;
        BackgroundOps.execute(() -> handwrittenNoteRepository.getNoteImage(atomicNote, true).noteImage,
                (bitmapOpt) -> {
                    if (bitmapOpt.isPresent()) {
                        bitmapOpt.ifPresent(drawingView::setBitmap);
                        return;
                    }
                    Bitmap newBitmap;
                    if (useDefaultBitmap()) {
                        newBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                    } else {
                        newBitmap = Bitmap.createBitmap(drawingView.currentWidth, drawingView.currentHeight, Bitmap.Config.ARGB_8888);
                    }
                    BackgroundOps.execute(() -> handwrittenNoteRepository.saveHandwrittenNoteImage(atomicNote, newBitmap));
                    drawingView.setBitmap(newBitmap);
                });

        BackgroundOps.execute(() -> handwrittenNoteRepository.getPageTemplate(atomicNote),
                pageTemplateOpt -> {
                    if (pageTemplateOpt.isPresent()) {
                        pageTemplateOpt.ifPresent(drawingView::setPageTemplate);
                        return;
                    }
                    PageTemplate pageTemplate = configReader.getAppConfig().getPageTemplates()
                            .get(PageBackgroundType.BASIC_RULED_PAGE_TEMPLATE.name());
                    BackgroundOps.execute(() -> handwrittenNoteRepository.saveHandwrittenNotePageTemplate(atomicNote, pageTemplate));

                    drawingView.setPageTemplate(pageTemplate);
                });
    }

    @Override
    public void saveNote() {
        BackgroundOps.execute(() ->
                handwrittenNoteRepository.saveHandwrittenNotes(bookId,
                        atomicNote,
                        drawingView.getBitmap(),
                        drawingView.getPageTemplate()));
    }

    public boolean useDefaultBitmap() {
        return drawingView.currentWidth * drawingView.currentHeight == 0;
    }
}
