//package com.example.hellodroid;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.io.File;
//
//public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
//    private File[] notes;
//    private OnNoteClickListener listener;
//
//    public interface OnNoteClickListener {
//        void onNoteClick(File noteFile);
//    }
//
//    public NoteAdapter(File[] notes, OnNoteClickListener listener) {
//        this.notes = notes;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View itemView = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_note, parent, false);
//        return new NoteViewHolder(itemView);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
//        File note = notes[position];
//        holder.noteName.setText(note.getName());
//    }
//
//    @Override
//    public int getItemCount() {
//        return notes.length;
//    }
//
//    public void updateNotes(File[] newNotes) {
//        this.notes = newNotes;
//        notifyDataSetChanged();
//    }
//
//    class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        TextView noteName;
//
//        public NoteViewHolder(@NonNull View itemView) {
//            super(itemView);
//            noteName = itemView.findViewById(R.id.text_note_name);
//            itemView.setOnClickListener(this);
//        }
//
//        @Override
//        public void onClick(View v) {
//            listener.onNoteClick(notes[getAdapterPosition()]);
//        }
//    }
//}
