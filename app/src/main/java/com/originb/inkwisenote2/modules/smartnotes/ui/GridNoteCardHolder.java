package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.DateTimeUtils;
import com.originb.inkwisenote2.common.BitmapScale;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.repositories.*;
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import android.view.animation.LinearInterpolator;
import android.util.Log;
import android.animation.ObjectAnimator;

public class GridNoteCardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    @Getter
    private View itemView;

    private final SmartNoteGridAdapter smartNoteGridAdapter;
    private ComponentActivity parentActivity;

    private final ImageView noteImage;
    private final TextView textPreview;
    private final TextView noteTitle;
    private final ImageButton deleteBtn;
    private final ImageView noteStatusImg;
    private final ImageView relationViewBtn;
    private Animation rotateAnimation;

    private SmartNotebook smartNotebook;
    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private final TextNotesDao textNotesDao;
    private final SmartNotebookRepository smartNotebookRepository;
    private final NoteRelationRepository noteRelationRepository;

    private ObjectAnimator rotateAnimator;

    public GridNoteCardHolder(SmartNoteGridAdapter smartNoteGridAdapter, @NonNull @NotNull View itemView,
                              ComponentActivity parentActivity) {
        super(itemView);
        this.smartNoteGridAdapter = smartNoteGridAdapter;
        this.parentActivity = parentActivity;
        this.itemView = itemView;

        noteImage = itemView.findViewById(R.id.card_image);
        textPreview = itemView.findViewById(R.id.note_text_preview);
        noteTitle = itemView.findViewById(R.id.card_name);
        deleteBtn = itemView.findViewById(R.id.btn_dlt_note);
        relationViewBtn = itemView.findViewById(R.id.btn_relation_view);
        noteStatusImg = itemView.findViewById(R.id.img_note_status);
        rotateAnimation = AnimationUtils.loadAnimation(parentActivity, R.anim.anim_rotate);

        noteImage.setOnClickListener(view -> onClick(itemView));
        textPreview.setOnClickListener(view -> onClick(itemView));
        deleteBtn.setOnClickListener(view -> onClickDelete());
        relationViewBtn.setVisibility(View.GONE);

        handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
        textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        noteRelationRepository = Repositories.getInstance().getNoteRelationRepository();

        initializeAnimation();
    }

    public void setNote(SmartNotebook smartNotebook) {
        this.smartNotebook = smartNotebook;
        String noteTitle = Optional.ofNullable(smartNotebook.getSmartBook().getTitle())
                .filter(title -> !title.trim().isEmpty())
                .orElse(DateTimeUtils.msToDateTime(smartNotebook.getSmartBook().getLastModifiedTimeMillis()));
        this.noteTitle.setText(noteTitle);

        int numberOfNotes = smartNotebook.getAtomicNotes().size();
        if (numberOfNotes == 0) return;
        AtomicNoteEntity firstNote = smartNotebook.getAtomicNotes().get(0);

        if (NoteType.TEXT_NOTE.equals(firstNote.getNoteType())) {
            BackgroundOps.execute(() ->
                            textNotesDao.getTextNoteForNote(firstNote.getNoteId()),
                    (textNote) -> {
                        noteImage.setVisibility(View.GONE);
                        textPreview.setVisibility(View.VISIBLE);
                        if(textNote != null) {
                            textPreview.setText(textNote.getNoteText());
                        }
                    });
        } else {
            BackgroundOps.execute(() ->
                            handwrittenNoteRepository.getNoteImage(firstNote, BitmapScale.THUMBNAIL),
                    (handwrittenNoteWithImage) ->
                            handwrittenNoteWithImage.noteImage.ifPresent(noteImage::setImageBitmap));
        }
    }

    public void updateNoteStatus(Events.NoteStatus noteStatus) {
        // Always cleanup any existing animation first
        stopRotationAnimation();
        
        if (!TextProcessingStage.NOTE_READY.equals(noteStatus.status)) {
            // Start new animation for processing state
            noteStatusImg.setImageResource(R.drawable.ic_in_process);
            
            // Create rotation animation programmatically
            rotateAnimator = ObjectAnimator.ofFloat(
                    noteStatusImg,
                    "rotation",
                    0f, 360f
            );
            rotateAnimator.setDuration(1000); // 1 second per rotation
            rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            rotateAnimator.setInterpolator(new LinearInterpolator());
            
            // Start animation
            rotateAnimator.start();
            
            Log.d("GridNoteCardHolder", "Starting rotation animation");
        } else {
            // Set ready status image without animation
            noteStatusImg.setImageResource(R.drawable.ic_tick_circle);
            Log.d("GridNoteCardHolder", "Set to ready status");
        }
    }
    
    /**
     * Helper method to stop rotation animation and reset view state
     */
    private void stopRotationAnimation() {
        Log.d("GridNoteCardHolder", "Stopping rotation animation");
        
        if (rotateAnimator != null) {
            try {
                rotateAnimator.cancel();
                rotateAnimator.removeAllListeners();
                rotateAnimator = null;
            } catch (Exception e) {
                Log.e("GridNoteCardHolder", "Error stopping animator", e);
            }
        }
        
        try {
            noteStatusImg.clearAnimation();
            noteStatusImg.animate().cancel();
            noteStatusImg.setRotation(0f); // Reset rotation
        } catch (Exception e) {
            Log.e("GridNoteCardHolder", "Error resetting image view", e);
        }
    }

    public int updateNoteRelation(boolean isRelated) {
        if (!isRelated) {
            relationViewBtn.setVisibility(View.GONE);
        } else {
            relationViewBtn.setVisibility(View.VISIBLE);
            relationViewBtn.setOnClickListener(v ->
                    Routing.RelatedNotesActivity
                            .openRelatedNotesIntent(parentActivity, smartNotebook.getSmartBook().getBookId()));
        }
        return getAdapterPosition();
    }

    private void onClickDelete() {
        // Show confirmation dialog
        new AlertDialog.Builder(parentActivity)
                .setTitle("Delete Notebook")
                .setMessage("Are you sure you want to delete this notebook? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteNotebook())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User cancelled, do nothing
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteNotebook() {
        BackgroundOps.execute(() -> {
            smartNotebook.atomicNotes.forEach(note -> {
                handwrittenNoteRepository.deleteHandwrittenNote(note);
                noteRelationRepository.deleteNoteRelationData(note);
            });
            smartNotebookRepository.deleteSmartNotebook(smartNotebook);
        });

        smartNoteGridAdapter.removeSmartNotebook(getAdapterPosition());
    }

    @Override
    public void onClick(View v) {
//        if (smartNotebook.getAtomicNotes().get(0).getNoteType().equals(NoteType.TEXT_NOTE.toString())) {
//            Routing.TextNoteActivity.openNotebookIntent(parentActivity,
//                    parentActivity.getFilesDir().getPath(),
//                    smartNotebook.getSmartBook().getBookId());
//        } else {
        Routing.SmartNotebookActivity.openNotebookIntent(parentActivity,
                parentActivity.getFilesDir().getPath(),
                smartNotebook.getSmartBook().getBookId());
//        }
    }

    private void initializeAnimation() {
        try {
            rotateAnimation = AnimationUtils.loadAnimation(parentActivity, R.anim.anim_rotate);
            if (rotateAnimation == null) {
                Log.e("GridNoteCardHolder", "Failed to load rotation animation");
                return;
            }
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            Log.d("GridNoteCardHolder", "Animation initialized successfully");
        } catch (Exception e) {
            Log.e("GridNoteCardHolder", "Error initializing animation", e);
        }
    }

    public void onViewRecycled() {
        stopRotationAnimation();
    }


}
