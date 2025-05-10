package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.originb.inkwisenote2.R;

import com.originb.inkwisenote2.config.ConfigReader;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.handwrittennotes.PageBackgroundType;

import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate;
import com.originb.inkwisenote2.modules.handwrittennotes.ui.DrawingView;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;

import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;

import org.greenrobot.eventbus.EventBus;

/**
 * Fragment for displaying and editing handwritten notes
 */
public class HandwrittenNoteFragment extends NoteFragment {

    private View fragmentView;
    private DrawingView drawingView;
    private ImageButton deleteNote;
    private ImageButton debugButton;
    private ImageButton eraserButton;
    private ImageButton pencilButton;

    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private ConfigReader configReader;


    public HandwrittenNoteFragment(SmartNotebook smartNotebook, AtomicNoteEntity atomicNote) {
        super(smartNotebook, atomicNote);
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
        eraserButton = fragmentView.findViewById(R.id.eraser_button);
        pencilButton = fragmentView.findViewById(R.id.pencil_button);

        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Delete note button listener
        deleteNote.setOnClickListener(v -> {
            BackgroundOps.execute(() -> {
                EventBus.getDefault().post(new Events.DeleteNoteCommand(
                        smartNotebook,
                        atomicNote
                ));
            });
        });

        // Debug button listener
        debugButton.setOnClickListener(v -> {
            showDebugDialog();
        });

        // Set up eraser button listener
        eraserButton.setOnClickListener(v -> {
            activateEraserMode();
        });

        // Set up pencil button listener
        pencilButton.setOnClickListener(v -> {
            activatePencilMode();
        });

        // Start in pencil mode by default
        activatePencilMode();

        loadNote();
    }

    /**
     * Switch to eraser mode
     */
    private void activateEraserMode() {
        if (drawingView != null) {
            drawingView.setEraserMode(true);

            // Highlight eraser button, unhighlight pencil button
            eraserButton.setAlpha(1.0f);
            pencilButton.setAlpha(0.5f);
        }
    }

    /**
     * Switch to pencil (drawing) mode
     */
    private void activatePencilMode() {
        if (drawingView != null) {
            drawingView.setEraserMode(false);

            // Highlight pencil button, unhighlight eraser button
            pencilButton.setAlpha(1.0f);
            eraserButton.setAlpha(0.5f);
        }
    }

    protected void loadNote() {
        if (atomicNote == null) return;
        
        // Make sure we have the correct filepath based on notebook title
        String notebookTitle = smartNotebook.getSmartBook().getTitle();
        String baseDir = atomicNote.getFilepath();
        if (baseDir.lastIndexOf('/') > 0) {
            baseDir = baseDir.substring(0, baseDir.lastIndexOf('/'));
        }
        
        // If notebook has a title, ensure it's used as the directory name
        if (notebookTitle != null && !notebookTitle.trim().isEmpty()) {
            String correctPath = baseDir + "/" + notebookTitle;
            if (!atomicNote.getFilepath().equals(correctPath)) {
                atomicNote.setFilepath(correctPath);
            }
        } else {
            // If no title, use timestamp
            String timestamp = String.valueOf(smartNotebook.getSmartBook().getLastModifiedTimeMillis());
            String correctPath = baseDir + "/" + timestamp;
            if (!atomicNote.getFilepath().equals(correctPath)) {
                atomicNote.setFilepath(correctPath);
            }
        }

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
