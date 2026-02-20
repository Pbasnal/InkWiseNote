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
    RecyclerView.Adapter<QueryResultsAdapter.QueryViewHolder?>() {
    private var queryNames: MutableList<String?>
    private var queryResults: MutableMap<String?, MutableSet<QueryNoteResult?>?>? = null


    private val viewHolderPositionMap: MutableMap<Int?, QueryViewHolder?>

    init {
        this.queryNames = ArrayList<String?>()
        this.viewHolderPositionMap = HashMap<Int?, QueryViewHolder?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueryViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.item_query_results, parent, false)

        return QueryViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueryViewHolder, position: Int) {
        val queryName = queryNames.get(position)
        holder.queryName.setText(queryName)
        holder.setQueryName(queryName, activity)

        val results = queryResults!!.get(queryName)
        if (results != null) {
            holder.notesAdapter.setNotes(queryName, results)
        }

        viewHolderPositionMap.put(position, holder)

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
            viewHolderPositionMap.get(position)!!.expand()
        }
    }

    fun setPositionCollapsed(position: Int) {
        if (notEmpty(viewHolderPositionMap) && viewHolderPositionMap.containsKey(position)) {
            viewHolderPositionMap.get(position)!!.collapse()
        }
    }

    override fun getItemCount(): Int {
        return queryNames.size
    }

    fun setData(results: MutableMap<String?, MutableSet<QueryNoteResult?>?>) {
        this.queryNames = ArrayList<String?>(results.keys)
        this.queryResults = results
        notifyDataSetChanged()
    }

    internal class QueryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var queryName: TextView
        var expandQueryResults: ImageButton
        var resultsRecyclerView: RecyclerView
        var notesAdapter: NotesAdapter
        var toggleButton: ImageButton

        init {
            queryName = itemView.findViewById<TextView>(R.id.query_name)
            resultsRecyclerView = itemView.findViewById<RecyclerView>(R.id.query_results_recycler)
            expandQueryResults = itemView.findViewById<ImageButton>(R.id.open_query_results_btn)

            toggleButton = itemView.findViewById<ImageButton>(R.id.query_results_toggle)
            toggleButton.setOnClickListener(View.OnClickListener { v: View? -> toggle() })

            // Set up horizontal scrolling for results
            resultsRecyclerView.setLayoutManager(
                LinearLayoutManager(
                    itemView.getContext(),
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
            resultsRecyclerView.setVisibility(View.VISIBLE)
            toggleButton.setImageResource(R.drawable.toggle_expanded)
        }

        fun collapse() {
            resultsRecyclerView.setVisibility(View.GONE)
            toggleButton.setImageResource(R.drawable.toggle_collapsed)
        }

        fun toggle() {
            val visibility = resultsRecyclerView.getVisibility()
            when (visibility) {
                View.VISIBLE -> collapse()
                else -> expand()
            }
        }
    }
}