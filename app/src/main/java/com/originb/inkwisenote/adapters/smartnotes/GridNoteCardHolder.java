package com.originb.inkwisenote.adapters.smartnotes;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.commonutils.DateTimeUtils;
import com.originb.inkwisenote.constants.BitmapScale;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.modules.messaging.BackgroundOps;
import com.originb.inkwisenote.modules.repositories.*;
import com.originb.inkwisenote.ux.Routing;
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

        int numberOfNotes = smartNotebook.getAtomicNotes().size();
        if (numberOfNotes == 1 && smartNotebook.getAtomicNotes().get(0).getNoteType().equals("handwritten_png")) {
            AtomicNoteEntity atomicNote = smartNotebook.getAtomicNotes().get(0);

            BackgroundOps.execute(() ->
                            handwrittenNoteRepository.getNoteImage(atomicNote, BitmapScale.THUMBNAIL),
                    (handwrittenNoteWithImage) ->
                            handwrittenNoteWithImage.noteImage.ifPresent(noteImage::setImageBitmap));
        }

        String noteTitle = Optional.ofNullable(smartNotebook.getSmartBook().getTitle())
                .filter(title -> !title.trim().isEmpty())
                .orElse(DateTimeUtils.msToDateTime(smartNotebook.getSmartBook().getLastModifiedTimeMillis()));
        this.noteTitle.setText(noteTitle);
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
