package com.originb.inkwisenote2.modules.smarthome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.Routing;

import java.util.*;

public class QueryResultsAdapter extends RecyclerView.Adapter<QueryResultsAdapter.QueryViewHolder> {
    private AppCompatActivity activity;
    private List<String> queryNames;
    private Map<String, Set<QueryNoteResult>> queryResults;

    private MutableLiveData<Boolean> isExpandedLive = new MutableLiveData<>(false);
    private ImageButton toggleButton;

    public QueryResultsAdapter(AppCompatActivity activity) {
        this.activity = activity;
        this.queryNames = new ArrayList<>();
    }

    @NonNull
    @Override
    public QueryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_query_results, parent, false);

        toggleButton = view.findViewById(R.id.query_results_toggle);
        toggleButton.setOnClickListener(v -> {
            isExpandedLive.setValue(Boolean.FALSE.equals(isExpandedLive.getValue()));
        });

        return new QueryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueryViewHolder holder, int position) {
        String queryName = queryNames.get(position);
        holder.queryName.setText(queryName);

        holder.setQueryName(queryName, activity);

        Set<QueryNoteResult> results = queryResults.get(queryName);
        if (results != null) {
            holder.notesAdapter.setNotes(queryName, results);
        }

        if (position == 0) {
            holder.expand();
            isExpandedLive.setValue(true);
        } else {
            holder.collapse();
            isExpandedLive.setValue(false);
        }

        isExpandedLive.observe(activity, isExpanded -> {
            if (isExpanded) {
                toggleButton.setImageResource(R.drawable.toggle_expanded);
                holder.expand();
            } else {
                toggleButton.setImageResource(R.drawable.toggle_collapsed);
                holder.collapse();
            }
        });
    }

    @Override
    public int getItemCount() {
        return queryNames.size();
    }

    public void setData(Map<String, Set<QueryNoteResult>> results) {
        this.queryNames = new ArrayList<>(results.keySet());
        this.queryResults = results;
        notifyDataSetChanged();
    }

    static class QueryViewHolder extends RecyclerView.ViewHolder {
        TextView queryName;
        ImageButton imageButton;
        RecyclerView resultsRecyclerView;
        NotesAdapter notesAdapter;

        QueryViewHolder(View itemView) {
            super(itemView);
            queryName = itemView.findViewById(R.id.query_name);
            resultsRecyclerView = itemView.findViewById(R.id.query_results_recycler);
            imageButton = itemView.findViewById(R.id.open_query_results_btn);

            // Set up horizontal scrolling for results
            resultsRecyclerView.setLayoutManager(
                    new LinearLayoutManager(itemView.getContext(),
                            LinearLayoutManager.VERTICAL, false));

            notesAdapter = new NotesAdapter(itemView.getContext());
            resultsRecyclerView.setAdapter(notesAdapter);
        }

        public void setQueryName(String queryName, Context packageContext) {
            imageButton.setOnClickListener(v -> {
                Routing.QueryActivity.openQueryResultsActivity(packageContext, queryName);
            });
        }

        public void expand() {
            resultsRecyclerView.setVisibility(View.VISIBLE);
        }

        public void collapse() {
            resultsRecyclerView.setVisibility(View.GONE);
        }
    }
} 