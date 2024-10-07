package com.originb.inkwisenote.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.activities.NoteActivity;
import com.originb.inkwisenote.data.NoteEntity;
import com.originb.inkwisenote.data.NoteMeta;
import com.originb.inkwisenote.data.repositories.NoteRepository;
import com.originb.inkwisenote.io.NoteMetaFiles;
import com.originb.inkwisenote.io.sql.NoteTextContract;
import com.originb.inkwisenote.modules.Repositories;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NoteGridAdapter extends RecyclerView.Adapter<NoteGridAdapter.NoteCardHolder> {

    private ComponentActivity parentActivity;
    private NoteRepository noteRepository;
    private NoteMetaFiles noteMetaRepository;
    private NoteTextContract.NoteTextDbHelper noteTextDbHelper;

    private List<Long> noteIds;


    public NoteGridAdapter(ComponentActivity parentActivity, List<Long> noteIds) {
        this.noteRepository = Repositories.getInstance().getNoteRepository();
        this.noteMetaRepository = Repositories.getInstance().getNoteMetaRepository();
        this.noteTextDbHelper = Repositories.getInstance().getNoteTextDbHelper();

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
                    noteRepository.getThumbnail(noteEntity.getNoteId())
                            .ifPresent(noteCardHolder.noteImage::setImageBitmap);
                    noteCardHolder.noteTitle.setText(noteEntity.getNoteMeta().getNoteTitle());
                });
    }

    @Override
    public int getItemCount() {
        return noteIds.size();
    }

    public class NoteCardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ComponentActivity parentActivity;

        private final ImageView noteImage;
        private final TextView noteTitle;
        private ImageButton deleteBtn;

        public NoteCardHolder(@NonNull @NotNull View itemView, ComponentActivity parentActivity) {
            super(itemView);
            this.parentActivity = parentActivity;

            noteImage = itemView.findViewById(R.id.card_image);
            noteTitle = itemView.findViewById(R.id.card_name);
            deleteBtn = itemView.findViewById(R.id.btn_dlt_note);

            noteImage.setOnClickListener(view -> onClick(itemView));
            deleteBtn.setOnClickListener(view -> {
                int position = getAdapterPosition();
                Long noteId = noteIds.get(position);
                // delete note files
                noteRepository.deleteNote(noteId);

                // delete note search text
                NoteTextContract.NoteTextQueries.deleteNoteText(noteId, noteTextDbHelper);

                // delete note from list
                noteIds.remove(position);
//                noteIds = Arrays.stream(noteMetaRepository.getAllNoteIds()).collect(Collectors.toList());
                notifyItemRemoved(position);
            });
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Long noteId = noteIds.get(position);

            Optional<NoteEntity> noteEntityOpt = noteRepository.getNoteEntity(noteId);
            noteEntityOpt.ifPresent(noteEntity -> {
                Intent intent = new Intent(parentActivity, NoteActivity.class);
                NoteActivity.openNoteIntent(intent, parentActivity.getFilesDir().getPath(),
                        noteEntity.getNoteId(),
                        noteEntity.getNoteMeta().getNoteFileName());
                parentActivity.startActivity(intent);
            });

            if(!noteEntityOpt.isPresent()) {
                // delete note from list
                noteIds.remove(position);
                notifyItemRemoved(position);
            }
        }
    }
}
