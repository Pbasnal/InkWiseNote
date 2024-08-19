package com.originb.inkwisenote;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class NoteGridAdapter extends RecyclerView.Adapter<NoteGridAdapter.NoteCardHolder> {
    private File[] notes;
    private OnNoteClickListener listener;

    public NoteGridAdapter(File[] files, OnNoteClickListener listener) {
        this.notes = files;
        this.listener = listener;
    }


    public interface OnNoteClickListener {
        void onNoteClick(File noteFile);
    }

    @NonNull
    @NotNull
    @Override
    public NoteCardHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);
        return new NoteGridAdapter.NoteCardHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NoteGridAdapter.NoteCardHolder noteCardHolder, int position) {
        File note = notes[position];
        Bitmap noteThumbnail = NoteRepository.getScaledBitmap(note.getAbsolutePath() + ".png", 150, 150);
//        Bitmap noteThumbnail = NoteRepository.loadBitmapWithCompletePath(note.getAbsolutePath());
        noteCardHolder.noteImage.setImageBitmap(noteThumbnail);
        noteCardHolder.noteTitle.setText(note.getName().split("\\.")[0]);
    }

    @Override
    public int getItemCount() {
        return notes.length;
    }

    public void updateNotes(File[] newNotes) {
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    public class NoteCardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView noteImage;
        private final TextView noteTitle;

        public NoteCardHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            noteImage = itemView.findViewById(R.id.card_image);
            noteTitle = itemView.findViewById(R.id.card_name);

            noteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onNoteClick(notes[getAdapterPosition()]);
                }
            });
        }

        @Override
        public void onClick(View v) {
            listener.onNoteClick(notes[getAdapterPosition()]);
        }
    }
}
