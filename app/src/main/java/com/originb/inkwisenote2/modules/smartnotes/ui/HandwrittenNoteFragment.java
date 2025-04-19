package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.BitmapScale;
import com.originb.inkwisenote2.config.ConfigReader;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.handwrittennotes.PageBackgroundType;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate;
import com.originb.inkwisenote2.modules.handwrittennotes.ui.DrawingView;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import org.greenrobot.eventbus.EventBus;

public class HandwrittenNoteFragment extends NoteFragment {

    private View fragmentView;
    private DrawingView drawingView;
    private ImageButton deleteNote;

    private long bookId;

    private final SmartNotebookRepository smartNotebookRepository;
    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private ConfigReader configReader;


    public HandwrittenNoteFragment() {
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
        configReader = ConfigReader.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.note_drawing_fragment, container, false);
        drawingView = fragmentView.findViewById(R.id.smart_drawing_view);
        deleteNote = fragmentView.findViewById(R.id.delete_note);

        deleteNote.setOnClickListener(v -> {
            BackgroundOps.execute(() ->
                    EventBus.getDefault().post(new Events.NoteDeleted(
                            smartNotebookRepository.getSmartNotebooks(bookId).get(),
                            atomicNote
                    )));

        });

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

        return fragmentView;
    }

    @Override
    public NoteHolderData getNoteHolderData() {
        return NoteHolderData.handWrittenNoteData(drawingView.getBitmap(), drawingView.getPageTemplate());
    }

    public boolean useDefaultBitmap() {
        return drawingView.currentWidth * drawingView.currentHeight == 0;
    }
}
