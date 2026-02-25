package com.originb.inkwisenote2.modules.smarthome

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.MapsUtils.notEmpty
import com.originb.inkwisenote2.common.Routing.QueryActivity.openQueryResultsActivity

class QueryResultsAdapter(private val activity: AppCompatActivity) :
    RecyclerView.Adapter<QueryResultsAdapter.QueryViewHolder>() {
    private var queryNames: MutableList<String>
    private var queryResults: MutableMap<String, MutableSet<QueryNoteResult>> = mutableMapOf()


    private val viewHolderPositionMap: MutableMap<Int, QueryViewHolder>

    init {
        this.queryNames = ArrayList<String>()
        this.viewHolderPositionMap = HashMap<Int, QueryViewHolder>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueryViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.item_query_results, parent, false)

        return QueryViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueryViewHolder, position: Int) {
        val queryName = queryNames[position]
        holder.queryName.text = queryName
        holder.setQueryName(queryName, activity)

        val results = queryResults[queryName]
        if (results != null) {
            holder.notesAdapter.setNotes(queryName, results)
        }

        viewHolderPositionMap[position] = holder

        if (position == 0) {
            setPositionExpanded(position)
        } else {
            setPositionCollapsed(position)
        }
    }

    fun setPositionExpanded(position: Int) {
        if (notEmpty(viewHolderPositionMap)
            && viewHolderPositionMap.containsKey(position)
        ) {
            viewHolderPositionMap[position]!!.expand()
        }
    }

    fun setPositionCollapsed(position: Int) {
        if (notEmpty(viewHolderPositionMap) && viewHolderPositionMap.containsKey(position)) {
            viewHolderPositionMap[position]!!.collapse()
        }
    }

    override fun getItemCount(): Int {
        return queryNames.size
    }

    fun setData(results: MutableMap<String, MutableSet<QueryNoteResult>>) {
        this.queryNames = ArrayList<String>(results.keys)
        this.queryResults = results
        notifyDataSetChanged()
    }

    class QueryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var queryName: TextView = itemView.findViewById<TextView>(R.id.query_name)
        var expandQueryResults: ImageButton = itemView.findViewById<ImageButton>(R.id.open_query_results_btn)
        var resultsRecyclerView: RecyclerView = itemView.findViewById<RecyclerView>(R.id.query_results_recycler)
        var notesAdapter: NotesAdapter
        var toggleButton: ImageButton = itemView.findViewById<ImageButton>(R.id.query_results_toggle)

        init {
            toggleButton.setOnClickListener(View.OnClickListener { v: View? -> toggle() })

            // Set up horizontal scrolling for results
            resultsRecyclerView.setLayoutManager(
                LinearLayoutManager(
                    itemView.context,
                    LinearLayoutManager.VERTICAL, false
                )
            )

            notesAdapter = NotesAdapter(itemView.getContext())
            resultsRecyclerView.setAdapter(notesAdapter)
        }

        fun setQueryName(queryName: String?, packageContext: Context) {
            expandQueryResults.setOnClickListener(View.OnClickListener { v: View? ->
                openQueryResultsActivity(packageContext, queryName)
            })
        }

        fun expand() {
            resultsRecyclerView.visibility = View.VISIBLE
            toggleButton.setImageResource(R.drawable.toggle_expanded)
        }

        fun collapse() {
            resultsRecyclerView.visibility = View.GONE
            toggleButton.setImageResource(R.drawable.toggle_collapsed)
        }

        fun toggle() {
            val visibility = resultsRecyclerView.visibility
            when (visibility) {
                View.VISIBLE -> collapse()
                else -> expand()
            }
        }
    }
}