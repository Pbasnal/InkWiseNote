package com.originb.inkwisenote2.modules.smartnotes.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.common.ScreenUtils.pxToDp
import com.originb.inkwisenote2.config.AppState
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteStatus
import com.originb.inkwisenote2.modules.backgroundjobs.Events.SmartNotebookSaved
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.java.KoinJavaComponent.get
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet

class SmartNoteGridAdapter(
    private val parentActivity: ComponentActivity,
    private var smartNotebooks: MutableList<SmartNotebook>,
    isCompact: Boolean
) : RecyclerView.Adapter<GridNoteCardHolder?>() {
    private val logger = Logger("SmartNoteGridAdapter")

    private val handwrittenNoteRepository: HandwrittenNoteRepository?
    private val textNotesDao: TextNotesDao?
    private val smartNotebookRepository: SmartNotebookRepository?
    private val noteRelationRepository: NoteRelationRepository?

    private val bookRelationMap: MutableMap<Long?, Boolean?> = HashMap<Long?, Boolean?>()

    private val bookCards: MutableMap<Long?, GridNoteCardHolder?> = HashMap<Long?, GridNoteCardHolder?>()

    private var isCompact = false

    init {
        this.isCompact = isCompact

        // Get dependencies from Koin
        this.handwrittenNoteRepository = get<HandwrittenNoteRepository?>(HandwrittenNoteRepository::class.java)
        this.textNotesDao = get<TextNotesDao?>(TextNotesDao::class.java)
        this.smartNotebookRepository = get<SmartNotebookRepository?>(SmartNotebookRepository::class.java)
        this.noteRelationRepository = get<NoteRelationRepository?>(NoteRelationRepository::class.java)

        AppState.observeNoteRelationships(
            parentActivity,
            { updatedNoteRelationMap: MutableSet<NoteRelation?> -> this.updateNoteRelations(updatedNoteRelationMap) })
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNoteStatusChange(noteStatus: NoteStatus) {
        val bookId = noteStatus.bookId

        if (bookCards.containsKey(bookId)) {
            val holder = bookCards.get(bookId)
            holder!!.updateNoteStatus(noteStatus)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSmartNotebookSaved(smartNotebookSaved: SmartNotebookSaved) {
        val savedNotebook = smartNotebookSaved.smartNotebook
        val bookId: Long = savedNotebook.getSmartBook().getBookId()

        if (bookCards.containsKey(bookId)) {
            val holder = bookCards.get(bookId)
            holder!!.setNote(savedNotebook)
        }
    }

    fun updateNoteRelations(updatedNoteRelationMap: MutableSet<NoteRelation?>) {
        logger.debug("Updating note relations", updatedNoteRelationMap)

        val relatedBookIds: MutableSet<Long?> = updatedNoteRelationMap.stream()
            .map<Any?>(NoteRelation::getBookId)
            .collect(Collectors.toSet())
        relatedBookIds.addAll(
            updatedNoteRelationMap.stream()
                .map<Any?>(NoteRelation::getRelatedBookId)
                .collect(Collectors.toSet())
        )

        for (bookId in bookCards.keys) {
            val isBookRelated = relatedBookIds.contains(bookId)
            val bookHolder = bookCards.get(bookId)
            bookHolder!!.updateNoteRelation(isBookRelated)
            bookRelationMap.put(bookId, isBookRelated)
        }

        // sets that the book is related even if bookCards haven't been loaded
        for (bookId in relatedBookIds) {
            bookRelationMap.put(bookId, true)
        }
    }

    fun setSmartNotebooks(smartNotebooks: MutableList<SmartNotebook?>) {
        this.smartNotebooks = ArrayList<SmartNotebook>(smartNotebooks)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): GridNoteCardHolder {
        val itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.card_layout, parent, false)

        return GridNoteCardHolder(
            this, itemView, parentActivity,
            handwrittenNoteRepository, textNotesDao,
            smartNotebookRepository, noteRelationRepository
        )
    }

    override fun onBindViewHolder(gridNoteCardHolder: GridNoteCardHolder, position: Int) {
        val smartNotebook = smartNotebooks.get(position)

        if (isCompact) {
            val params = gridNoteCardHolder.getItemView().getLayoutParams()
            params.height = pxToDp(200, parentActivity)
            params.width = pxToDp(200, parentActivity)
            gridNoteCardHolder.getItemView().setLayoutParams(params)
        }

        logger.debug("Setting book at position: " + position, smartNotebook.getSmartBook())
        gridNoteCardHolder.setNote(smartNotebook)
        bookCards.put(smartNotebook.getSmartBook().getBookId(), gridNoteCardHolder)

        val bookId: Long = smartNotebook.getSmartBook().getBookId()
        if (!bookRelationMap.containsKey(bookId)) {
            logger.debug("Book doesn't have any relations yet. bookId: " + bookId)
            return
        }

        gridNoteCardHolder.updateNoteRelation(bookRelationMap.get(bookId)!!)
    }

    // Callback when an item is detached (item goes out of view)
    override fun onViewDetachedFromWindow(holder: GridNoteCardHolder) {
        super.onViewDetachedFromWindow(holder)
        val position = holder.getAdapterPosition()

        if (position < 0 || smartNotebooks.size <= position) return

        val index = holder.getAdapterPosition() - 1
        if (index < 0 || index >= smartNotebooks.size) return
        val smartNotebook = smartNotebooks.get(index)

        bookCards.remove(smartNotebook.getSmartBook().getBookId())
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
