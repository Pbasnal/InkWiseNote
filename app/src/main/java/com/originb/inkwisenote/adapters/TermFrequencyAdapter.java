package com.originb.inkwisenote.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.data.admin.TermFrequencyEntry;

import java.util.ArrayList;
import java.util.List;

public class TermFrequencyAdapter extends RecyclerView.Adapter<TermFrequencyAdapter.ViewHolder> {
    private List<TermFrequencyEntry> termFrequencies = new ArrayList<>();

    public void setTermFrequencies(List<TermFrequencyEntry> termFrequencies) {
        this.termFrequencies = termFrequencies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_term_frequency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TermFrequencyEntry entry = termFrequencies.get(position);
        holder.noteIdText.setText("Note ID: " + entry.getNoteId());
        holder.termText.setText("Term: " + entry.getTerm());
        holder.frequencyText.setText("Frequency: " + entry.getFrequency());
    }

    @Override
    public int getItemCount() {
        return termFrequencies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView noteIdText;
        TextView termText;
        TextView frequencyText;

        ViewHolder(View itemView) {
            super(itemView);
            noteIdText = itemView.findViewById(R.id.note_id_text);
            termText = itemView.findViewById(R.id.term_text);
            frequencyText = itemView.findViewById(R.id.frequency_text);
        }
    }
} 