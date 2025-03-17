package com.originb.inkwisenote2.modules.smartnotes.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.config.AppState
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteStatus
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.stream.Collectors

class SmartNoteGridAdapter(
    private val parentActivity: ComponentActivity,
    private var smartNotebooks: List<SmartNotebook>
) : RecyclerView.Adapter<GridNoteCardHolder>() {
    private val logger = Logger("SmartNoteGridAdapter")

    private val bookRelationMap: MutableMap<Long, Boolean> = HashMap()

    private val bookCards: MutableMap<Long, GridNoteCardHolder> = HashMap()

    init {
        AppState.Companion.observeNoteRelationships(
            parentActivity,
            Observer<MutableSet<NoteRelation?>> { updatedNoteRelationMap: Set<NoteRelation?> ->
                this.updateNoteRelations(updatedNoteRelationMap)
            })
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNoteStatusChange(noteStatus: NoteStatus) {
        val bookId = noteStatus.smartNotebook.getSmartBook().bookId

        if (bookCards.containsKey(bookId)) {
            val holder = bookCards[bookId]
            holder!!.updateNoteStatus(noteStatus)
        }
    }

    fun updateNoteRelations(updatedNoteRelationMap: Set<NoteRelation?>) {
        logger.debug("Updating note relations", updatedNoteRelationMap)

        val relatedBookIds = updatedNoteRelationMap.stream()
            .map { obj: NoteRelation? -> obj.getBookId() }
            .collect(Collectors.toSet())
        relatedBookIds.addAll(updatedNoteRelationMap.stream()
            .map { obj: NoteRelation? -> obj.getRelatedBookId() }
            .collect(Collectors.toSet()))

        for (bookId in bookCards.keys) {
            val isBookRelated = relatedBookIds.contains(bookId)
            val bookHolder = bookCards[bookId]
            bookHolder!!.updateNoteRelation(isBookRelated)
            bookRelationMap[bookId] = isBookRelated
        }

        // sets that the book is related even if bookCards haven't been loaded
        for (bookId in relatedBookIds) {
            bookRelationMap[bookId] = true
        }
    }

    fun setSmartNotebooks(smartNotebooks: List<SmartNotebook>?) {
        this.smartNotebooks = ArrayList(smartNotebooks)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): GridNoteCardHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_layout, parent, false)

        return GridNoteCardHolder(this, itemView, parentActivity)
    }

    override fun onBindViewHolder(gridNoteCardHolder: GridNoteCardHolder, position: Int) {
        val smartNotebook = smartNotebooks[position]

        logger.debug("Setting book at position: $position", smartNotebook.getSmartBook())
        gridNoteCardHolder.setNote(smartNotebook)
        bookCards[smartNotebook.getSmartBook().bookId] = gridNoteCardHolder

        val bookId = smartNotebook.getSmartBook().bookId
        if (!bookRelationMap.containsKey(bookId)) {
            logger.debug("Book doesn't have any relations yet. bookId: $bookId")
            return
        }

        gridNoteCardHolder.updateNoteRelation(bookRelationMap[bookId]!!)
    }

    // Callback when an item is detached (item goes out of view)
    override fun onViewDetachedFromWindow(holder: GridNoteCardHolder) {
        super.onViewDetachedFromWindow(holder)
        val position = holder.adapterPosition

        if (position < 0 || smartNotebooks.size <= position) return

        val smartNotebook = smartNotebooks[holder.adapterPosition - 1]

        bookCards.remove(smartNotebook.getSmartBook().bookId)
    }

    override fun getItemCount(): Int {
        return smartNotebooks.size
    }

    fun removeSmartNotebook(adapterPosition: Int) {
        smartNotebooks.removeAt(adapterPosition)
        notifyItemRemoved(adapterPosition)
    }

    override fun onViewRecycled(holder: GridNoteCardHolder) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }
}
