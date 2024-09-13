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
import com.originb.inkwisenote.data.Note;
import com.originb.inkwisenote.io.BitmapRepository;
import com.originb.inkwisenote.modules.Repositories;
import org.jetbrains.annotations.NotNull;
import com.originb.inkwisenote.io.NoteRepository;

import java.util.Map;

public class NoteGridAdapter extends RecyclerView.Adapter<NoteGridAdapter.NoteCardHolder> {
    //    private Map<Long, String> noteIdToNameMap;
//    private Long[] noteIds;
    private ComponentActivity parentActivity;
    private NoteRepository noteRepository;
    private BitmapRepository bitmapRepository;

    public NoteGridAdapter(ComponentActivity parentActivity) {
        this.noteRepository = Repositories.getInstance().getNotesRepository();
        this.bitmapRepository = Repositories.getInstance().getBitmapRepository();

//        this.noteIdToNameMap = noteRepository.getAllNoteNames();
//        this.noteIds = noteIdToNameMap.keySet().toArray(new Long[0]);

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
        Note note = noteRepository.getNoteAtIndex(position);
        bitmapRepository.getThumbnail(note.getNoteId())
                .ifPresent(noteCardHolder.noteImage::setImageBitmap);

        noteCardHolder.noteTitle.setText(note.getNoteName());
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
                Note note = noteRepository.getNoteAtIndex(position);
                noteRepository.deleteNoteFromDisk(note.getNoteId());
                bitmapRepository.deleteBitmap(note.getNoteId());

                notifyItemRemoved(position);
            });
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Note note = noteRepository.getNoteAtIndex(position);
            Intent intent = new Intent(parentActivity, NoteActivity.class);
            NoteActivity.openNoteIntent(intent, parentActivity.getFilesDir().getPath(),
                    note.getNoteId(),
                    note.getNoteName());
            parentActivity.startActivity(intent);
        }
    }
}
