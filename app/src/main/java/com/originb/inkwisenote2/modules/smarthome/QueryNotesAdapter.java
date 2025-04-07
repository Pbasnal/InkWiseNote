package com.originb.inkwisenote2.modules.smarthome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.BitmapFileIoUtils;
import com.originb.inkwisenote2.common.BitmapScale;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QueryNotesAdapter extends RecyclerView.Adapter<QueryNotesAdapter.NoteViewHolder> {
    private Context context;
    private List<AtomicNoteEntity> notes;
    private SimpleDateFormat dateFormat;

    public QueryNotesAdapter(Context context) {
        this.context = context;
        this.notes = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_query_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        AtomicNoteEntity note = notes.get(position);

        // Set note timestamp
        String timestamp = dateFormat.format(new Date(note.getCreatedTimeMillis()));
        holder.timestamp.setText(timestamp);

        // Load thumbnail (you'll need to implement this based on your image loading logic)
        loadThumbnail(holder.thumbnail, note.getFilepath());
    }

    private void loadThumbnail(ImageView imageView, String filepath) {
        BitmapFileIoUtils.readBitmapFromFile(filepath, BitmapScale.THUMBNAIL)
                .ifPresent(imageView::setImageBitmap);
        // Implement your image loading logic here
        // You might want to use Glide or Picasso for efficient image loading
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setNotes(List<AtomicNoteEntity> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView timestamp;

        NoteViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.note_thumbnail);
            timestamp = itemView.findViewById(R.id.note_timestamp);
        }
    }
} 