package com.originb.inkwisenote2.modules.smarthome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;

import java.util.*;

public class QueryResultsAdapter extends RecyclerView.Adapter<QueryResultsAdapter.QueryViewHolder> {
    private Context context;
    private List<String> queryNames;
    private Map<String, Set<QueryNoteResult>> queryResults;

    public QueryResultsAdapter(Context context) {
        this.context = context;
        this.queryNames = new ArrayList<>();
    }

    @NonNull
    @Override
    public QueryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_query_results, parent, false);
        return new QueryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueryViewHolder holder, int position) {
        String queryName = queryNames.get(position);
        holder.queryName.setText(queryName);

        Set<QueryNoteResult> results = queryResults.get(queryName);
        if (results != null) {
            holder.notesAdapter.setNotes(results);
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
        RecyclerView resultsRecyclerView;
        NotesAdapter notesAdapter;

        QueryViewHolder(View itemView) {
            super(itemView);
            queryName = itemView.findViewById(R.id.query_name);
            resultsRecyclerView = itemView.findViewById(R.id.query_results_recycler);

            // Set up horizontal scrolling for results
            resultsRecyclerView.setLayoutManager(
                    new LinearLayoutManager(itemView.getContext(),
                            LinearLayoutManager.VERTICAL, false));

            notesAdapter = new NotesAdapter(itemView.getContext());
            resultsRecyclerView.setAdapter(notesAdapter);
        }
    }
} 