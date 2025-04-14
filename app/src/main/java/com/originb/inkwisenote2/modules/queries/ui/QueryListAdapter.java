package com.originb.inkwisenote2.modules.queries.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;

public class QueryListAdapter extends ListAdapter<QueryEntity, QueryListAdapter.QueryViewHolder> {
    private final OnQueryClickListener listener;

    public QueryListAdapter(OnQueryClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public QueryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_query, parent, false);
        return new QueryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueryViewHolder holder, int position) {
        QueryEntity query = getItem(position);
        holder.bind(query, listener);
    }

    static class QueryViewHolder extends RecyclerView.ViewHolder {
        private final TextView queryName;
        private final TextView findWords;
        private final TextView ignoreWords;
        private final ImageButton editButton;

        QueryViewHolder(@NonNull View itemView) {
            super(itemView);
            queryName = itemView.findViewById(R.id.query_name);
            findWords = itemView.findViewById(R.id.find_words);
            ignoreWords = itemView.findViewById(R.id.ignore_words);
            editButton = itemView.findViewById(R.id.edit_button);
        }

        void bind(QueryEntity query, OnQueryClickListener listener) {
            queryName.setText(query.getName());
            findWords.setText("Find: " + query.getWordsToFind());
            ignoreWords.setText("Ignore: " + query.getWordsToIgnore());
            
            itemView.setOnClickListener(v -> listener.onQueryClick(query));
            editButton.setOnClickListener(v -> listener.onEditClick(query));
        }
    }

    private static final DiffUtil.ItemCallback<QueryEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<QueryEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull QueryEntity oldItem,
                                             @NonNull QueryEntity newItem) {
                    return oldItem.getName() == newItem.getName();
                }

                @Override
                public boolean areContentsTheSame(@NonNull QueryEntity oldItem,
                                                @NonNull QueryEntity newItem) {
                    return oldItem.getName().equals(newItem.getName()) &&
                           oldItem.getWordsToFind().equals(newItem.getWordsToFind()) &&
                           oldItem.getWordsToIgnore().equals(newItem.getWordsToIgnore());
                }
            };

    public interface OnQueryClickListener {
        void onQueryClick(QueryEntity query);
        void onEditClick(QueryEntity query);
    }
} 