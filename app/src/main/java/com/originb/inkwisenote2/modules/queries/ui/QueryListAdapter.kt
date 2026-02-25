package com.originb.inkwisenote2.modules.queries.ui

import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.isNullOrWhitespace
import com.originb.inkwisenote2.modules.queries.data.QueryEntity

class QueryListAdapter(private val listener: OnQueryClickListener) :
    ListAdapter<QueryEntity, QueryListAdapter.QueryViewHolder?>(
        DIFF_CALLBACK
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_query, parent, false)
        return QueryViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueryViewHolder, position: Int) {
        val query = getItem(position)
        holder.bind(query, listener)
    }

    class QueryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val queryName: TextView = itemView.findViewById<TextView>(R.id.query_name)
        private val queryInfo: TextView = itemView.findViewById<TextView>(R.id.query_info)
        private val editButton: ImageButton = itemView.findViewById<ImageButton>(R.id.edit_button)
        private val deleteButton: ImageButton = itemView.findViewById<ImageButton>(R.id.delete_query_btn)

        fun bind(query: QueryEntity, listener: OnQueryClickListener) {
            queryName.setText(query.name)
            queryName.setText(
                Html.fromHtml(
                    "Query <b>" + query.name + "</b>",
                    Html.FROM_HTML_MODE_LEGACY
                )
            )

            val queryInfoText: Spanned?
            if (isNullOrWhitespace(query.wordsToIgnore) && isNullOrWhitespace(query.wordsToFind)) {
                queryInfoText = getTextIfQueryDoesntHaveWords(query)
            } else if (isNullOrWhitespace(query.wordsToIgnore)) {
                queryInfoText = getTextIfOnlyFindWordsArePresent(query)
            } else if (isNullOrWhitespace(query.wordsToFind)) {
                queryInfoText = getTextIfOnlyIgnoreWordsArePresent(query)
            } else {
                queryInfoText = getTextIfBothFindAndIgnoreWordsArePresent(query)
            }

            queryInfo.text = queryInfoText

            itemView.setOnClickListener(View.OnClickListener { v: View? -> listener.onQueryClick(query) })
            editButton.setOnClickListener(View.OnClickListener { v: View? -> listener.onEditClick(query) })
            deleteButton.setOnClickListener(View.OnClickListener { v: View? -> listener.onDeleteClick(query) })
        }

        private fun getTextIfBothFindAndIgnoreWordsArePresent(query: QueryEntity): Spanned? {
            return Html.fromHtml(
                ("will find notes which contains <b>" + query.wordsToFind + "</b> words "
                        + "but doesn't contains words: " + query.wordsToIgnore),
                Html.FROM_HTML_MODE_LEGACY
            )
        }

        private fun getTextIfOnlyFindWordsArePresent(query: QueryEntity): Spanned {
            return Html.fromHtml(
                "will find notes which contains <b>" + query.wordsToFind + "</b>",
                Html.FROM_HTML_MODE_LEGACY
            )
        }

        private fun getTextIfOnlyIgnoreWordsArePresent(query: QueryEntity): Spanned {
            return Html.fromHtml(
                "will find notes which do not contain <b>" + query.wordsToIgnore + "</b> words",
                Html.FROM_HTML_MODE_LEGACY
            )
        }

        private fun getTextIfQueryDoesntHaveWords(query: QueryEntity?): Spanned {
            return Html.fromHtml(
                "will not be executed since it doesn't contain any words",
                Html.FROM_HTML_MODE_LEGACY
            )
        }
    }

    interface OnQueryClickListener {
        fun onQueryClick(query: QueryEntity?)

        fun onEditClick(query: QueryEntity?)

        fun onDeleteClick(query: QueryEntity?)
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<QueryEntity?> =
            object : DiffUtil.ItemCallback<QueryEntity?>() {
                override fun areItemsTheSame(
                    oldItem: QueryEntity,
                    newItem: QueryEntity
                ): Boolean {
                    return oldItem.name === newItem.name
                }

                override fun areContentsTheSame(
                    oldItem: QueryEntity,
                    newItem: QueryEntity
                ): Boolean {
                    return oldItem.name == newItem.name &&
                            oldItem.wordsToFind == newItem.wordsToFind &&
                            oldItem.wordsToIgnore == newItem.wordsToIgnore
                }
            }
    }
}