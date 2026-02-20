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
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.common.Routing.SmartNotebookActivity.openNotebookIntent
import com.originb.inkwisenote2.common.Strings.focusedOnWord
import com.originb.inkwisenote2.functionalUtils.Try.Companion.to
import com.originb.inkwisenote2.modules.smarthome.NotesAdapter.NoteViewHolder
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import java.lang.String
import java.util.function.Function
import java.util.stream.Collectors
import kotlin.Any
import kotlin.Int
import kotlin.Long
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.MutableSet

class NotesAdapter(private val context: Context) : RecyclerView.Adapter<NoteViewHolder?>() {
    private var notes: MutableList<QueryNoteResult>
    private val logger = Logger("NotesAdapter")
    private var queryName: String? = ""

    init {
        this.notes = ArrayList<QueryNoteResult>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val queryResult = notes.get(position)
        holder.timestamp.setText(msToDateTime(queryResult.getLastModifiedMillis()))
        val focusedString = focusedOnWord(queryResult.getNoteText(), queryResult.getQueryWord())

        holder.noteWords.setText(focusedString)
        if (NoteType.TEXT_NOTE == queryResult.getNoteType()) {
            holder.thumbnail.setVisibility(View.GONE)
        } else {
            holder.thumbnail.setVisibility(View.VISIBLE)
            holder.thumbnail.setImageBitmap(queryResult.getNoteImage())
        }


        holder.itemView.setOnClickListener(View.OnClickListener { v: View? ->
            val noteIds = notes.stream().map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }.collect(
                Collectors.toSet()
            )
            val commaSeparatedNoteIds = noteIds.stream() // Convert set to stream
                .map<String?> { obj: Function<in R?, out V?>? -> String.valueOf(obj) }  // Map each Long to String
                .collect(Collectors.joining(","))

            val selectedNote = notes.get(position)
            if (position < notes.size) {
                val selectedNoteId = notes.get(position).getNoteId()
                to<Any?>(
                    Runnable {
                        openNotebookIntent(
                            context,
                            context.getFilesDir().getPath(),
                            queryName,
                            commaSeparatedNoteIds,
                            selectedNoteId
                        )
                    },
                    logger
                )
                    .get()
            } else {
                to<Any?>(
                    Runnable {
                        openNotebookIntent(
                            context,
                            context.getFilesDir().getPath(),
                            queryName,
                            commaSeparatedNoteIds
                        )
                    },
                    logger
                )
                    .get()
            }
        })
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun setNotes(queryName: kotlin.String?, notes: MutableSet<QueryNoteResult?>) {
        this.queryName = queryName
        this.notes = ArrayList<QueryNoteResult>(notes)
        notifyDataSetChanged()
    }

    internal class NoteViewHolder(var itemView: View) : RecyclerView.ViewHolder(itemView) {
        var noteWords: TextView
        var thumbnail: ImageView
        var timestamp: TextView

        init {
            noteWords = itemView.findViewById<TextView>(R.id.note_words)
            thumbnail = itemView.findViewById<ImageView>(R.id.note_thumbnail)
            timestamp = itemView.findViewById<TextView>(R.id.note_timestamp)
        }
    }
}