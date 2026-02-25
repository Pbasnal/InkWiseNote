package com.originb.inkwisenote2.modules.smarthome

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.DateTimeUtils.msToDateTime
import com.originb.inkwisenote2.common.Routing.SmartNotebookActivity.openNotebookIntent
import com.originb.inkwisenote2.common.focusedOnWord
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType

class NotesAdapter(private val context: Context) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {
    private var notes: MutableList<QueryNoteResult>
    private var queryName: String = ""

    init {
        this.notes = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val queryResult = notes[position]
        holder.timestamp.text = msToDateTime(queryResult.lastModifiedMillis)
        val focusedString = focusedOnWord(queryResult.noteText, queryResult.queryWord)
        holder.noteWords.text = focusedString
        if (NoteType.TEXT_NOTE == queryResult.noteType) {
            holder.thumbnail.visibility = View.GONE
        } else {
            holder.thumbnail.visibility = View.VISIBLE
            holder.thumbnail.setImageBitmap(queryResult.noteImage)
        }

        holder.itemView.setOnClickListener {
            val commaSeparatedNoteIds = notes.map { it.noteId }.joinToString(",")
            if (position < notes.size) {
                val selectedNoteId = notes[position].noteId
                openNotebookIntent(
                    context,
                    context.filesDir.path,
                    queryName,
                    commaSeparatedNoteIds,
                    selectedNoteId
                )
            } else {
                openNotebookIntent(
                    context,
                    context.filesDir.path,
                    queryName,
                    commaSeparatedNoteIds
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun setNotes(queryName: String, notes: MutableSet<QueryNoteResult>) {
        this.queryName = queryName
        this.notes = notes.toMutableList()
        notifyDataSetChanged()
    }

    class NoteViewHolder(var itemView: View) : RecyclerView.ViewHolder(itemView) {
        var noteWords: TextView = itemView.findViewById<TextView>(R.id.note_words)
        var thumbnail: ImageView = itemView.findViewById<ImageView>(R.id.note_thumbnail)
        var timestamp: TextView = itemView.findViewById<TextView>(R.id.note_timestamp)
    }
}