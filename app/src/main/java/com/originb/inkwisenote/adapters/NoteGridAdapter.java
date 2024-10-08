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
import com.originb.inkwisenote.data.NoteMeta;
import com.originb.inkwisenote.data.repositories.NoteRepository;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NoteGridAdapter extends RecyclerView.Adapter<NoteGridAdapter.NoteCardHolder> {

    private ComponentActivity parentActivity;
    private NoteRepository noteRepository;

    public NoteGridAdapter(ComponentActivity parentActivity) {
        this.noteRepository = new NoteRepository();

        this.parentActivity = parentActivity;
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
        NoteMeta noteMeta = noteRepository.getNoteAtIndex(position);
        noteRepository.getThumbnail(noteMeta.getNoteId())
                .ifPresent(noteCardHolder.noteImage::setImageBitmap);

        noteCardHolder.noteTitle.setText(noteMeta.getNoteTitle());
    }

    @Override
    public int getItemCount() {
        return noteRepository.numberOfNotes();
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
                 noteRepository.deleteNoteAtIndex(position);
                notifyItemRemoved(position);
            });
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            NoteMeta noteMeta = noteRepository.getNoteAtIndex(position);
            Intent intent = new Intent(parentActivity, NoteActivity.class);
            NoteActivity.openNoteIntent(intent, parentActivity.getFilesDir().getPath(),
                    noteMeta.getNoteId(),
                    noteMeta.getNoteFileName());
            parentActivity.startActivity(intent);
        }
    }
}
