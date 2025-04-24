package com.originb.inkwisenote2.modules.queries.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.R;

public class QueryWordAdapter extends ListAdapter<String, QueryWordAdapter.WordViewHolder> {
    private final OnWordClickListener listener;

    public QueryWordAdapter(OnWordClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        String word = getItem(position);
        holder.bind(word, listener);
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        private final TextView wordText;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.word_text);
        }

        void bind(String word, OnWordClickListener listener) {
            wordText.setText(word);
            itemView.setOnClickListener(v -> listener.onWordClick(word));
        }
    }

    private static final DiffUtil.ItemCallback<String> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<String>() {
                @Override
                public boolean areItemsTheSame(@NonNull String oldItem,
                                             @NonNull String newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull String oldItem,
                                                @NonNull String newItem) {
                    return oldItem.equals(newItem);
                }
            };

    interface OnWordClickListener {
        void onWordClick(String word);
    }
} 