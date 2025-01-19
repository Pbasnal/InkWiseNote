package com.originb.inkwisenote.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.data.backgroundjobs.TextProcessingStage;
import com.originb.inkwisenote.ux.utils.Routing;
import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.modules.repositories.NoteRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NoteGridAdapter extends RecyclerView.Adapter<NoteGridAdapter.NoteCardHolder> {

    private ComponentActivity parentActivity;
    private NoteRepository noteRepository;

    private List<Long> noteIds;

    private Map<Long, NoteCardHolder> noteCards = new HashMap<>();

    public NoteGridAdapter(ComponentActivity parentActivity, List<Long> noteIds) {
        this.noteRepository = Repositories.getInstance().getNoteRepository();

        this.parentActivity = parentActivity;
        this.noteIds = noteIds;
    }

    public void setNoteIds(List<Long> noteIds) {
        this.noteIds = noteIds;
        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public NoteCardHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);

        return new NoteGridAdapter.NoteCardHolder(itemView, parentActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NoteGridAdapter.NoteCardHolder noteCardHolder, int position) {
        Long noteId = noteIds.get(position);
        Optional<NoteEntity> noteEntityOpt = noteRepository.getNoteEntity(noteId);
        noteEntityOpt.ifPresent(noteEntity -> {
            noteCardHolder.setNote(noteEntity);
            noteCards.put(noteEntity.getNoteId(), noteCardHolder);
        });
    }

    public void updateCardStatus(Long noteId, TextProcessingStage noteStatus) {
        if (noteCards.containsKey(noteId)) {
            NoteCardHolder noteCard = noteCards.get(noteId);
            noteCard.updateNoteStatus(noteId, noteStatus);
        }
    }

    @Override
    public int getItemCount() {
        return noteIds.size();
    }

    public class NoteCardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ComponentActivity parentActivity;

        private final ImageView noteImage;
        private final TextView noteTitle;
        private final ImageButton deleteBtn;
        private final ImageButton graphButton;
        private final ImageView noteStatusImg;
        private Animation rotateAnimation;
        private NoteEntity noteEntity;

        private boolean isAnimationRunning = false;

        public NoteCardHolder(@NonNull @NotNull View itemView,
                              ComponentActivity parentActivity) {
            super(itemView);
            this.parentActivity = parentActivity;

            noteImage = itemView.findViewById(R.id.card_image);
            noteTitle = itemView.findViewById(R.id.card_name);
            deleteBtn = itemView.findViewById(R.id.btn_dlt_note);
            graphButton = itemView.findViewById(R.id.btn_graph_view);
            noteStatusImg = itemView.findViewById(R.id.img_note_status);

            rotateAnimation = AnimationUtils.loadAnimation(parentActivity, R.anim.anim_rotate);

            noteImage.setOnClickListener(view -> onClick(itemView));
            deleteBtn.setOnClickListener(view -> onClickDelete());
            graphButton.setOnClickListener(view -> {
                int position = getAdapterPosition();
                Long noteId = noteIds.get(position);
                Routing.RelatedNotesActivity.openRelatedNotesIntent(parentActivity, noteId);
            });
        }

        public void setNote(NoteEntity noteEntity) {
            this.noteEntity = noteEntity;

            noteRepository.getThumbnail(noteEntity.getNoteId())
                    .ifPresent(noteImage::setImageBitmap);

            String noteTitle = Optional.ofNullable(noteEntity.getNoteMeta().getNoteTitle())
                    .filter(title -> !title.trim().isEmpty())
                    .orElse(noteEntity.getNoteMeta().getCreateDateTimeString());
            this.noteTitle.setText(noteTitle);
        }

        public void updateNoteStatus(Long noteId, TextProcessingStage noteStatus) {
            if (!noteId.equals(noteEntity.getNoteId())) {
                return;
            }

            if (TextProcessingStage.NOTE_READY != noteStatus) {
                if (!isAnimationRunning) {
                    noteStatusImg.startAnimation(rotateAnimation);
                    noteStatusImg.setImageResource(R.drawable.ic_in_process);
//                    noteStatusImg.clearAnimation();
                    isAnimationRunning = true;
                }
            } else {
                noteStatusImg.setImageResource(R.drawable.ic_tick_circle);
//                noteStatusImg.clearAnimation();
                isAnimationRunning = false;
            }
        }

        private void onClickDelete() {
            int position = getAdapterPosition();
            Long noteId = noteIds.get(position);
            // delete note files
            noteRepository.deleteNote(noteId);

            // delete note from list
            noteIds.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Long noteId = noteIds.get(position);

            Optional<NoteEntity> noteEntityOpt = noteRepository.getNoteEntity(noteId);
            noteEntityOpt.ifPresent(noteEntity -> {
                Routing.NoteActivity.openNoteIntent(parentActivity,
                        parentActivity.getFilesDir().getPath(),
                        noteEntity.getNoteId());
            });

            if (!noteEntityOpt.isPresent()) {
                // Because of some data error, a note which doesn't
                // exist can show up on the grid.
                // delete note from list
                noteIds.remove(position);
                notifyItemRemoved(position);
            }
        }
    }
}
