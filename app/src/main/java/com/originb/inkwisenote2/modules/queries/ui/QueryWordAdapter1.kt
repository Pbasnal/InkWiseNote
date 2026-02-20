package com.originb.inkwisenote2.modules.queries.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.queries.ui.QueryWordAdapter.WordViewHolder

class QueryWordAdapter(private val listener: OnWordClickListener) :
    ListAdapter<String?, WordViewHolder?>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = getItem(position)
        holder.bind(word, listener)
    }

    internal class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val wordText: TextView

        init {
            wordText = itemView.findViewById<TextView>(R.id.word_text)
        }

        fun bind(word: String?, listener: OnWordClickListener) {
            wordText.setText(word)
            itemView.setOnClickListener(View.OnClickListener { v: View? -> listener.onWordClick(word) })
        }
    }

    internal interface OnWordClickListener {
        fun onWordClick(word: String?)
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<String?> = object : DiffUtil.ItemCallback<String?>() {
            override fun areItemsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}