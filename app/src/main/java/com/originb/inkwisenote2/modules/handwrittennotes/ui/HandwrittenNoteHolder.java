package com.originb.inkwisenote2.modules.handwrittennotes.ui;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageButton;
import androidx.activity.ComponentActivity;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.config.ConfigReader;
import com.originb.inkwisenote2.common.BitmapScale;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.handwrittennotes.PageBackgroundType;
import com.originb.inkwisenote2.modules.smartnotes.ui.NoteHolder;
import org.greenrobot.eventbus.EventBus;

public class HandwrittenNoteHolder extends NoteHolder {

    private final DrawingView drawingView;
    private AtomicNoteEntity atomicNote;

    private ImageButton deleteNote;

    private long bookId;

    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private ConfigReader configReader;

    public HandwrittenNoteHolder(View itemView, ComponentActivity parentActivity, SmartNotebookRepository smartNotebookRepository) {
        super(itemView, parentActivity, smartNotebookRepository);
        drawingView = itemView.findViewById(R.id.smart_drawing_view);
        deleteNote = itemView.findViewById(R.id.delete_note);

        deleteNote.setOnClickListener(v -> {
            BackgroundOps.execute(() ->
                    EventBus.getDefault().post(new Events.NoteDeleted(
                            smartNotebookRepository.getSmartNotebooks(bookId).get(),
                            atomicNote
                    )));

        });

        handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
        configReader = ConfigReader.getInstance();
    }

    @Override
    public void setNote(long bookId, AtomicNoteEntity atomicNote) {
        this.bookId = bookId;
        this.atomicNote = atomicNote;
        BackgroundOps.execute(() -> handwrittenNoteRepository.getNoteImage(atomicNote, BitmapScale.FULL_SIZE).noteImage,
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
    public boolean saveNote() {
        // Todo: need to reload note images on home page once this is done

        BackgroundOps.execute(() ->
         handwrittenNoteRepository.saveHandwrittenNotes(bookId,
                atomicNote,
                drawingView.getBitmap(),
                drawingView.getPageTemplate())
         );

        return true;
    }

    public boolean useDefaultBitmap() {
        return drawingView.currentWidth * drawingView.currentHeight == 0;
    }
}
