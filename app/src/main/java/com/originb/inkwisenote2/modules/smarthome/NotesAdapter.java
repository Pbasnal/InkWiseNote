package com.originb.inkwisenote2.modules.smarthome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.*;
import com.originb.inkwisenote2.functionalUtils.Try;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private Context context;
    private List<QueryNoteResult> notes;
    private Logger logger = new Logger("NotesAdapter");
    ;

    public NotesAdapter(Context context) {
        this.context = context;
        this.notes = new ArrayList<>();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        QueryNoteResult queryResult = notes.get(position);
        holder.timestamp.setText(DateTimeUtils.msToDateTime(queryResult.getLastModifiedMillis()));
        String focusedString = Strings.focusedOnWord(queryResult.getNoteText(), queryResult.getQueryWord());

        holder.noteWords.setText(focusedString);
        if (NoteType.TEXT_NOTE.equals(queryResult.getNoteType())) {
            holder.thumbnail.setVisibility(View.GONE);
        } else {
            holder.thumbnail.setVisibility(View.VISIBLE);
            holder.thumbnail.setImageBitmap(queryResult.getNoteImage());
        }


        holder.itemView.setOnClickListener(v -> {
            Set<Long> noteIds = notes.stream().map(QueryNoteResult::getNoteId).collect(Collectors.toSet());
            String commaSeparatedNoteIds = noteIds.stream()  // Convert set to stream
                    .map(String::valueOf)  // Map each Long to String
                    .collect(Collectors.joining(","));
            Try.to(() -> Routing.SmartNotebookActivity.openNotebookIntent(context, context.getFilesDir().getPath(), commaSeparatedNoteIds), logger)
                    .get();
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setNotes(Set<QueryNoteResult> notes) {
        this.notes = new ArrayList<>(notes);
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView noteWords;
        ImageView thumbnail;
        TextView timestamp;

        NoteViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            noteWords = itemView.findViewById(R.id.note_words);
            thumbnail = itemView.findViewById(R.id.note_thumbnail);
            timestamp = itemView.findViewById(R.id.note_timestamp);
        }
    }
} 