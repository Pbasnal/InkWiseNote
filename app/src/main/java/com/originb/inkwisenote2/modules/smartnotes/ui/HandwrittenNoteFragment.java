package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote2.modules.smartnotes.ui.NoteFragment;

import org.greenrobot.eventbus.EventBus;

/**
 * Fragment for displaying and editing handwritten notes
 */
public class HandwrittenNoteFragment extends NoteFragment {

    private View fragmentView;
    private DrawingView drawingView;
    private ImageButton deleteNote;
    private ImageButton debugButton;

    private final SmartNotebookRepository smartNotebookRepository;
    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private ConfigReader configReader;


    public HandwrittenNoteFragment(SmartNotebook smartNotebook, AtomicNoteEntity atomicNote) {
        super(smartNotebook, atomicNote);
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
        debugButton = fragmentView.findViewById(R.id.debug_button);

        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        deleteNote.setOnClickListener(v -> {
            BackgroundOps.execute(() -> {
                EventBus.getDefault().post(new Events.NoteDeleted(
                        smartNotebook,
                        atomicNote
                ));
            });
        });
        debugButton.setOnClickListener(v -> {
            showDebugDialog();
        });
        loadNote();
    }

    protected void loadNote() {
        if (atomicNote == null) return;

        // Load the note image
        BackgroundOps.execute(
                () -> handwrittenNoteRepository.getNoteImage(atomicNote, BitmapScale.FULL_SIZE).noteImage,
                bitmapOpt -> {
                    if (bitmapOpt.isPresent()) {
                        if (drawingView != null) {
                            drawingView.setBitmap(bitmapOpt.get());
                        }
                        return;
                    }

                    // Create a new bitmap if none exists
                    Bitmap newBitmap;
                    if (useDefaultBitmap()) {
                        newBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                    } else {
                        newBitmap = Bitmap.createBitmap(
                                drawingView.currentWidth,
                                drawingView.currentHeight,
                                Bitmap.Config.ARGB_8888
                        );
                    }

                    if (drawingView != null) {
                        drawingView.setBitmap(newBitmap);
                    }

                    BackgroundOps.execute(() ->
                            handwrittenNoteRepository.saveHandwrittenNoteImage(atomicNote, newBitmap)
                    );
                }
        );

        // Load strokes from markdown file
        BackgroundOps.execute(
                () -> handwrittenNoteRepository.readHandwrittenNoteMarkdown(atomicNote),
                strokes -> {
                    if (drawingView != null && strokes != null && !strokes.isEmpty()) {
                        drawingView.setStrokes(strokes);
                    }
                }
        );

        // Load the page template
        BackgroundOps.execute(
                () -> handwrittenNoteRepository.getPageTemplate(atomicNote),
                pageTemplateOpt -> {
                    if (pageTemplateOpt.isPresent() && drawingView != null) {
                        drawingView.setPageTemplate(pageTemplateOpt.get());
                        return;
                    }

                    // Create a new page template if none exists
                    PageTemplate pageTemplate = configReader.getAppConfig().getPageTemplates()
                            .get(PageBackgroundType.BASIC_RULED_PAGE_TEMPLATE.name());

                    if (drawingView != null) {
                        drawingView.setPageTemplate(pageTemplate);
                    }

                    BackgroundOps.execute(() ->
                            handwrittenNoteRepository.saveHandwrittenNotePageTemplate(atomicNote, pageTemplate)
                    );
                }
        );
    }

    /**
     * Show the debug dialog with note information
     */
    private void showDebugDialog() {
        if (getContext() != null) {
            NoteDebugDialog dialog = new NoteDebugDialog(getContext(), atomicNote, smartNotebook);
            dialog.show();
        }
    }

    @Override
    public NoteHolderData getNoteHolderData() {
        if (drawingView == null) {
            return NoteHolderData.handWrittenNoteData(null, null);
        }

        return NoteHolderData.handWrittenNoteData(
                drawingView.getBitmap(),
                drawingView.getPageTemplate(),
                drawingView.getStrokes()
        );
    }

    private boolean useDefaultBitmap() {
        return drawingView == null || drawingView.currentWidth * drawingView.currentHeight == 0;
    }
}
