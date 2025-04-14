package com.originb.inkwisenote2.modules.queries.ui;

import android.text.Html;
import android.text.Spanned;
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
import com.originb.inkwisenote2.common.Strings;
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
        private final TextView queryInfo;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        QueryViewHolder(@NonNull View itemView) {
            super(itemView);
            queryName = itemView.findViewById(R.id.query_name);
            queryInfo = itemView.findViewById(R.id.query_info);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_query_btn);
        }

        void bind(QueryEntity query, OnQueryClickListener listener) {
            queryName.setText(query.getName());
            queryName.setText(Html.fromHtml("Query <b>" + query.getName() + "</b>",
                    Html.FROM_HTML_MODE_LEGACY));

            Spanned queryInfoText;
            if (Strings.isNullOrWhitespace(query.getWordsToIgnore()) && Strings.isNullOrWhitespace(query.getWordsToFind())) {
                queryInfoText = getTextIfQueryDoesntHaveWords(query);
            } else if (Strings.isNullOrWhitespace(query.getWordsToIgnore())) {
                queryInfoText = getTextIfOnlyFindWordsArePresent(query);
            } else if (Strings.isNullOrWhitespace(query.getWordsToFind())) {
                queryInfoText = getTextIfOnlyIgnoreWordsArePresent(query);
            } else {
                queryInfoText = getTextIfBothFindAndIgnoreWordsArePresent(query);
            }

            queryInfo.setText(queryInfoText);

            itemView.setOnClickListener(v -> listener.onQueryClick(query));
            editButton.setOnClickListener(v -> listener.onEditClick(query));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(query));
        }

        private Spanned getTextIfBothFindAndIgnoreWordsArePresent(QueryEntity query) {
            return Html.fromHtml("will find notes which contains <b>" + query.getWordsToFind() + "</b> words "
                            + "but doesn't contains words: " + query.getWordsToIgnore(),
                    Html.FROM_HTML_MODE_LEGACY);
        }

        private Spanned getTextIfOnlyFindWordsArePresent(QueryEntity query) {
            return Html.fromHtml("will find notes which contains <b>" + query.getWordsToFind() + "</b>",
                    Html.FROM_HTML_MODE_LEGACY);
        }

        private Spanned getTextIfOnlyIgnoreWordsArePresent(QueryEntity query) {
            return Html.fromHtml("will find notes which do not contain <b>" + query.getWordsToIgnore() + "</b> words",
                    Html.FROM_HTML_MODE_LEGACY);
        }

        private Spanned getTextIfQueryDoesntHaveWords(QueryEntity query) {
            return Html.fromHtml("will not be executed since it doesn't contain any words",
                    Html.FROM_HTML_MODE_LEGACY);
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

        void onDeleteClick(QueryEntity query);
    }
} 