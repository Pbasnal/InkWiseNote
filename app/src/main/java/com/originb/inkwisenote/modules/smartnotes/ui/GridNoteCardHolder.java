package com.originb.inkwisenote.modules.smartnotes.ui;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.common.DateTimeUtils;
import com.originb.inkwisenote.common.BitmapScale;
import com.originb.inkwisenote.modules.backgroundjobs.Events;
import com.originb.inkwisenote.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote.modules.repositories.*;
import com.originb.inkwisenote.common.Routing;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GridNoteCardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final SmartNoteGridAdapter smartNoteGridAdapter;
    private ComponentActivity parentActivity;

    private final ImageView noteImage;
    private final TextView noteTitle;
    private final ImageButton deleteBtn;
    private final ImageView noteStatusImg;
    private final ImageView relationViewBtn;
    private SmartNotebook smartNotebook;


    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private final SmartNotebookRepository smartNotebookRepository;
    private final NoteRelationRepository noteRelationRepository;

    public GridNoteCardHolder(SmartNoteGridAdapter smartNoteGridAdapter, @NonNull @NotNull View itemView,
                              ComponentActivity parentActivity) {
        super(itemView);
        this.smartNoteGridAdapter = smartNoteGridAdapter;
        this.parentActivity = parentActivity;

        noteImage = itemView.findViewById(R.id.card_image);
        noteTitle = itemView.findViewById(R.id.card_name);
        deleteBtn = itemView.findViewById(R.id.btn_dlt_note);
        relationViewBtn = itemView.findViewById(R.id.btn_relation_view);
        noteStatusImg = itemView.findViewById(R.id.img_note_status);

        noteImage.setOnClickListener(view -> onClick(itemView));
        deleteBtn.setOnClickListener(view -> onClickDelete());
        relationViewBtn.setVisibility(View.GONE);


        handwrittenNoteRepository = Repositories.getInstance().getHandwrittenNoteRepository();
        smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        noteRelationRepository = Repositories.getInstance().getNoteRelationRepository();
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

        if (!firstNote.getNoteType().equals("handwritten_png")) {
            return;
        }

        BackgroundOps.execute(() ->
                        handwrittenNoteRepository.getNoteImage(firstNote, BitmapScale.THUMBNAIL),
                (handwrittenNoteWithImage) ->
                        handwrittenNoteWithImage.noteImage.ifPresent(noteImage::setImageBitmap));
    }

    public void updateNoteStatus(Events.NoteStatus noteStatus) {

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
        EventBus.getDefault().post(new Events.NotebookDeleted(smartNotebook));
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
        Routing.SmartNotebookActivity.openNotebookIntent(parentActivity,
                parentActivity.getFilesDir().getPath(),
                smartNotebook.getSmartBook().getBookId());
    }


}
