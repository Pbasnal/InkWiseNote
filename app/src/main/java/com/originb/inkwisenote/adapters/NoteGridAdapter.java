package com.originb.inkwisenote.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.filemanager.BitmapFileManager;
import com.originb.inkwisenote.filemanager.FileInfo;
import com.originb.inkwisenote.repositories.NoteRepository;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.activities.NoteActivity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NoteGridAdapter extends RecyclerView.Adapter<NoteGridAdapter.NoteCardHolder> {
    private List<String> notes;
    private ComponentActivity parentActivity;
    private NoteRepository noteRepository;

    public NoteGridAdapter(List<String> files, ComponentActivity parentActivity, NoteRepository noteRepository) {
        this.notes = files;
        this.parentActivity = parentActivity;
        this.noteRepository = noteRepository;
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
        String note = notes.get(position);
        FileInfo<Bitmap> noteThumbnailInfo = noteRepository.getThumbnailInfo(note);
        BitmapFileManager.readBitmapFromFile(noteThumbnailInfo.filePath, 0.1f)
                .ifPresent(noteCardHolder.noteImage::setImageBitmap);

        noteCardHolder.noteTitle.setText(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void updateNotes(List<String> newNotes) {
        this.notes = newNotes;
        notifyDataSetChanged();
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
                noteRepository.deleteNoteFromDisk(notes.get(position));
                notes.remove(position);
                notifyItemRemoved(position);
            });
        }

        @Override
        public void onClick(View v) {
            String noteFile = notes.get(getAdapterPosition());
            // Open NoteActivity with the selected note
            Intent intent = new Intent(parentActivity, NoteActivity.class);
            intent.putExtra("noteFileName", noteFile);
            parentActivity.startActivity(intent);
        }
    }
}
