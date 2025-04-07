package com.originb.inkwisenote2.modules.smarthome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.modules.queries.data.QueryEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryResultsAdapter extends RecyclerView.Adapter<QueryResultsAdapter.QueryViewHolder> {
    private Context context;
    private List<String> queryNames;
    private Map<String, List<AtomicNoteEntity>> queryResults;

    public QueryResultsAdapter(Context context) {
        this.context = context;
        this.queryNames = new ArrayList<>();
        this.queryResults = new HashMap<>();
    }

    @Override
    public QueryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_query_results, parent, false);
        return new QueryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(QueryViewHolder holder, int position) {
        String queryName = queryNames.get(position);
        holder.queryName.setText(queryName);
        
        List<AtomicNoteEntity> notes = queryResults.get(queryName);
        if (notes != null) {
            holder.notesAdapter.setNotes(notes);
        }
    }

    @Override
    public int getItemCount() {
        return queryNames.size();
    }

    public void setData(List<String> queryNames, Map<String, List<AtomicNoteEntity>> results) {
        this.queryNames = queryNames;
        this.queryResults = results;
        notifyDataSetChanged();
    }

    class QueryViewHolder extends RecyclerView.ViewHolder {
        TextView queryName;
        RecyclerView notesRecyclerView;
        QueryNotesAdapter notesAdapter;

        QueryViewHolder(View itemView) {
            super(itemView);
            queryName = itemView.findViewById(R.id.query_name);
            notesRecyclerView = itemView.findViewById(R.id.notes_recycler_view);
            
            notesAdapter = new QueryNotesAdapter(context);
            notesRecyclerView.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            notesRecyclerView.setAdapter(notesAdapter);
        }
    }
} 