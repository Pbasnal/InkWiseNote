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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.data.backgroundjobs.TextProcessingStage;
import com.originb.inkwisenote.data.config.AppState;
import com.originb.inkwisenote.data.dao.NoteRelationDao;
import com.originb.inkwisenote.data.notedata.NoteRelation;
import com.originb.inkwisenote.ux.utils.Routing;
import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.modules.repositories.NoteRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class NoteGridAdapter extends RecyclerView.Adapter<NoteGridAdapter.NoteCardHolder> {

    private ComponentActivity parentActivity;
    private NoteRepository noteRepository;

    private List<Long> noteIds;

    private Map<Long, List<NoteRelation>> noteRelationMap = new HashMap<>();
    private Map<Long, NoteCardHolder> noteCards = new HashMap<>();

    public NoteGridAdapter(ComponentActivity parentActivity, List<Long> noteIds) {
        this.noteRepository = Repositories.getInstance().getNoteRepository();
        this.parentActivity = parentActivity;
        this.noteIds = noteIds;

        AppState.getInstance().observeNoteRelationships(parentActivity, this::updateNoteRelations);
    }

    public void updateNoteRelations(Map<Long, List<NoteRelation>> updatedNoteRelationMap) {
        for (Long noteId : updatedNoteRelationMap.keySet()) {
            List<NoteRelation> updatedRelations = updatedNoteRelationMap.getOrDefault(noteId, new ArrayList<>());
            boolean notify = !noteRelationMap.containsKey(noteId) && !updatedRelations.isEmpty();

            if (notify) {
                this.noteRelationMap.put(noteId, updatedRelations);
                int position = noteIds.indexOf(noteId);
                notifyItemChanged(position);
            }
        }

//            this.noteRelationMap = noteRelationShipMap;
//            notifyDataSetChanged();
    }

    public void setNoteIds(Set<Long> noteIds) {
        this.noteIds = new ArrayList<>(noteIds);

        Set<Long> noteIdsThatDontHaveRelationship = new HashSet<>();
        for (Long noteId : noteIds) {
            if (!noteRelationMap.containsKey(noteId)) {
                noteIdsThatDontHaveRelationship.add(noteId);
            }
        }

        notifyDataSetChanged();
        if (!noteIdsThatDontHaveRelationship.isEmpty()) {
            NoteRelationDao noteRelationDao = Repositories.getInstance().getNotesDb().noteRelationDao();

//            noteIdsThatDontHaveRelationship.forEach(noteIdToRefresh ->
//            {
//                noteRelationDao.getRelatedNotesOf(noteIdToRefresh).observe(parentActivity, allNoteRelations -> {
//                    AppState.getInstance().updatedRelatedNotes(noteIdToRefresh, allNoteRelations);
//                });
//            });


            noteRelationDao.getRelatedNotesOf(noteIdsThatDontHaveRelationship)
                    .observe(parentActivity, allNoteRelations -> {
                        Map<Long, List<NoteRelation>> allNoteRelationMap = allNoteRelations.stream()
                                .collect(Collectors.groupingBy(NoteRelation::getNoteId));

                        allNoteRelationMap.putAll(allNoteRelations.stream()
                                .collect(Collectors.groupingBy(NoteRelation::getRelatedNoteId)));
                        AppState.getInstance().updatedRelatedNotes(allNoteRelationMap);
                    });
        }
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


            if (noteRelationMap.containsKey(noteEntity.getNoteId())) {
                graphButton.setVisibility(View.VISIBLE); // Show the button
                graphButton.setEnabled(true);
            } else {
                graphButton.setVisibility(View.GONE);
                graphButton.setEnabled(false);
            }
        }

        public void updateNoteStatus(Long noteId, TextProcessingStage noteStatus) {
            if (!noteId.equals(noteEntity.getNoteId())) {
                return;
            }

            if (TextProcessingStage.NOTE_READY != noteStatus) {
                if (!isAnimationRunning) {
                    noteStatusImg.clearAnimation();
                    noteStatusImg.setImageResource(R.drawable.ic_in_process);
                    noteStatusImg.post(() -> noteStatusImg.startAnimation(rotateAnimation));
                    isAnimationRunning = true;
                }
            } else {
                noteStatusImg.setImageResource(R.drawable.ic_tick_circle);
                noteStatusImg.clearAnimation();
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
