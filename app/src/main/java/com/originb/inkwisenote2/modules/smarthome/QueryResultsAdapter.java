package com.originb.inkwisenote2.modules.smarthome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.MapsUtils;
import com.originb.inkwisenote2.common.Routing;

import java.util.*;

public class QueryResultsAdapter extends RecyclerView.Adapter<QueryResultsAdapter.QueryViewHolder> {
    private AppCompatActivity activity;
    private List<String> queryNames;
    private Map<String, Set<QueryNoteResult>> queryResults;


    private Map<Integer, QueryViewHolder> viewHolderPositionMap;

    public QueryResultsAdapter(AppCompatActivity activity) {
        this.activity = activity;
        this.queryNames = new ArrayList<>();
        this.viewHolderPositionMap = new HashMap<>();
    }

    @NonNull
    @Override
    public QueryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_query_results, parent, false);

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

        viewHolderPositionMap.put(position, holder);

        if (position == 0) {
            setPositionExpanded(position);
        } else {
            setPositionCollapsed(position);
        }
    }

    public void setPositionExpanded(int position) {
        if (MapsUtils.notEmpty(viewHolderPositionMap)
                && viewHolderPositionMap.containsKey(position)) {
            viewHolderPositionMap.get(position).expand();
        }
    }

    public void setPositionCollapsed(int position) {
        if (MapsUtils.notEmpty(viewHolderPositionMap) && viewHolderPositionMap.containsKey(position)) {
            viewHolderPositionMap.get(position).collapse();
        }
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
        ImageButton expandQueryResults;
        RecyclerView resultsRecyclerView;
        NotesAdapter notesAdapter;
        ImageButton toggleButton;

        public QueryViewHolder(View itemView) {
            super(itemView);
            queryName = itemView.findViewById(R.id.query_name);
            resultsRecyclerView = itemView.findViewById(R.id.query_results_recycler);
            expandQueryResults = itemView.findViewById(R.id.open_query_results_btn);

            toggleButton = itemView.findViewById(R.id.query_results_toggle);
            toggleButton.setOnClickListener(v -> toggle());

            // Set up horizontal scrolling for results
            resultsRecyclerView.setLayoutManager(
                    new LinearLayoutManager(itemView.getContext(),
                            LinearLayoutManager.VERTICAL, false));

            notesAdapter = new NotesAdapter(itemView.getContext());
            resultsRecyclerView.setAdapter(notesAdapter);
        }

        public void setQueryName(String queryName, Context packageContext) {
            expandQueryResults.setOnClickListener(v -> {
                Routing.QueryActivity.openQueryResultsActivity(packageContext, queryName);
            });
        }

        public void expand() {
            resultsRecyclerView.setVisibility(View.VISIBLE);
            toggleButton.setImageResource(R.drawable.toggle_expanded);
        }

        public void collapse() {
            resultsRecyclerView.setVisibility(View.GONE);
            toggleButton.setImageResource(R.drawable.toggle_collapsed);
        }

        public void toggle() {
            int visibility = resultsRecyclerView.getVisibility();
            switch (visibility) {
                case View.VISIBLE:
                    collapse();
                    break;
                default:
                    expand();
                    break;
            }
        }
    }
}