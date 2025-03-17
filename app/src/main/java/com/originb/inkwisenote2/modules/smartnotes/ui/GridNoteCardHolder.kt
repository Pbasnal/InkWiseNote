package com.originb.inkwisenote2.modules.smartnotes.ui

import android.animation.ObjectAnimator
import android.graphics.*
import android.util.Log
import android.view.View
import android.view.animation.*
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteStatus
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NotebookDeleted
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteWithImage
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer

class GridNoteCardHolder(
    private val smartNoteGridAdapter: SmartNoteGridAdapter, itemView: View,
    private val parentActivity: ComponentActivity
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    private val noteImage: ImageView = itemView.findViewById(R.id.card_image)
    private val textPreview: TextView = itemView.findViewById(R.id.note_text_preview)
    private val noteTitle: TextView = itemView.findViewById(R.id.card_name)
    private val deleteBtn: ImageButton = itemView.findViewById(R.id.btn_dlt_note)
    private val noteStatusImg: ImageView = itemView.findViewById(R.id.img_note_status)
    private val relationViewBtn: ImageView = itemView.findViewById(R.id.btn_relation_view)
    private var rotateAnimation: Animation?

    private var isAnimationRunning = false
    private var smartNotebook: SmartNotebook? = null
    private val handwrittenNoteRepository: HandwrittenNoteRepository
    private val textNotesDao: TextNotesDao
    private val smartNotebookRepository: SmartNotebookRepository
    private val noteRelationRepository: NoteRelationRepository

    private var rotateAnimator: ObjectAnimator? = null

    init {
        rotateAnimation = AnimationUtils.loadAnimation(parentActivity, R.anim.anim_rotate)

        noteImage.setOnClickListener { view: View? -> onClick(itemView) }
        textPreview.setOnClickListener { view: View? -> onClick(itemView) }
        deleteBtn.setOnClickListener { view: View? -> onClickDelete() }
        relationViewBtn.visibility = View.GONE

        handwrittenNoteRepository = Repositories.Companion.getInstance().getHandwrittenNoteRepository()
        textNotesDao = Repositories.Companion.getInstance().getNotesDb().textNotesDao()
        smartNotebookRepository = Repositories.Companion.getInstance().getSmartNotebookRepository()
        noteRelationRepository = Repositories.Companion.getInstance().getNoteRelationRepository()

        initializeAnimation()
    }

    fun setNote(smartNotebook: SmartNotebook) {
        this.smartNotebook = smartNotebook
        val noteTitle = Optional.ofNullable(smartNotebook.getSmartBook().title)
            .filter { title: String? -> !title!!.trim { it <= ' ' }.isEmpty() }
            .orElse(DateTimeUtils.msToDateTime(smartNotebook.getSmartBook().lastModifiedTimeMillis))
        this.noteTitle.text = noteTitle

        val numberOfNotes = smartNotebook.getAtomicNotes().size
        if (numberOfNotes == 0) return
        val firstNote = smartNotebook.getAtomicNotes()[0]

        if (NoteType.TEXT_NOTE.equals(firstNote.noteType)) {
            BackgroundOps.Companion.execute<TextNoteEntity?>(
                Callable<TextNoteEntity?> { textNotesDao.getTextNoteForBook(smartNotebook.getSmartBook().bookId) },
                Consumer<TextNoteEntity?> { textNote: TextNoteEntity? ->
                    noteImage.visibility = View.GONE
                    textPreview.visibility = View.VISIBLE
                    textPreview.text = textNote.getNoteText()
                })
        } else {
            BackgroundOps.Companion.execute<HandwrittenNoteWithImage?>(
                Callable<HandwrittenNoteWithImage?> {
                    handwrittenNoteRepository.getNoteImage(
                        firstNote,
                        BitmapScale.THUMBNAIL
                    )
                },
                Consumer<HandwrittenNoteWithImage?> { handwrittenNoteWithImage: HandwrittenNoteWithImage? ->
                    handwrittenNoteWithImage!!.noteImage!!.ifPresent { bm: Bitmap? ->
                        noteImage.setImageBitmap(
                            bm
                        )
                    }
                })
        }
    }

    fun updateNoteStatus(noteStatus: NoteStatus) {
        if (TextProcessingStage.NOTE_READY != noteStatus.status) {
            noteStatusImg.setImageResource(R.drawable.ic_in_process)

            // Create rotation animation programmatically
            rotateAnimator = ObjectAnimator.ofFloat(
                noteStatusImg,
                "rotation",
                0f, 360f
            )
            rotateAnimator.setDuration(1000) // 1 second per rotation
            rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE)
            rotateAnimator.setInterpolator(LinearInterpolator())

            // Start animation
            rotateAnimator.start()
            isAnimationRunning = true

            Log.d("GridNoteCardHolder", "Starting rotation animation")
        } else {
            // Stop animation and show ready status
            if (rotateAnimator != null) {
                noteStatusImg.post {
                    rotateAnimator!!.end()
                    rotateAnimator!!.removeAllListeners()
                    rotateAnimator!!.cancel()
                    rotateAnimator = null
                    noteStatusImg.clearAnimation() // Clear any remaining animations
                    noteStatusImg.animate().cancel() // Cancel any ongoing ViewPropertyAnimator
                    noteStatusImg.rotation = 0f // Reset rotation
                    noteStatusImg.setImageResource(R.drawable.ic_tick_circle)
                    isAnimationRunning = false
                }
            }

            Log.d("GridNoteCardHolder", "Stopping rotation animation")
        }
    }

    fun updateNoteRelation(isRelated: Boolean): Int {
        if (!isRelated) {
            relationViewBtn.visibility = View.GONE
        } else {
            relationViewBtn.visibility = View.VISIBLE
            relationViewBtn.setOnClickListener { v: View? ->
                Routing.RelatedNotesActivity.openRelatedNotesIntent(
                    parentActivity, smartNotebook.getSmartBook().bookId
                )
            }
        }
        return adapterPosition
    }

    private fun onClickDelete() {
        EventBus.getDefault().post(NotebookDeleted(smartNotebook))
        BackgroundOps.Companion.execute(Runnable {
            smartNotebook!!.atomicNotes!!.forEach(Consumer { note: AtomicNoteEntity? ->
                handwrittenNoteRepository.deleteHandwrittenNote(note)
                noteRelationRepository.deleteNoteRelationData(note)
            })
            smartNotebookRepository.deleteSmartNotebook(smartNotebook)
        })

        smartNoteGridAdapter.removeSmartNotebook(adapterPosition)
    }

    override fun onClick(v: View) {
//        if (smartNotebook.getAtomicNotes().get(0).getNoteType().equals(NoteType.TEXT_NOTE.toString())) {
//            Routing.TextNoteActivity.openNotebookIntent(parentActivity,
//                    parentActivity.getFilesDir().getPath(),
//                    smartNotebook.getSmartBook().getBookId());
//        } else {
        Routing.SmartNotebookActivity.openNotebookIntent(
            parentActivity,
            parentActivity.filesDir.path,
            smartNotebook.getSmartBook().bookId
        )
        //        }
    }

    private fun initializeAnimation() {
        try {
            rotateAnimation = AnimationUtils.loadAnimation(parentActivity, R.anim.anim_rotate)
            if (rotateAnimation == null) {
                Log.e("GridNoteCardHolder", "Failed to load rotation animation")
                return
            }
            rotateAnimation!!.repeatCount = Animation.INFINITE
            rotateAnimation!!.interpolator = LinearInterpolator()
            Log.d("GridNoteCardHolder", "Animation initialized successfully")
        } catch (e: Exception) {
            Log.e("GridNoteCardHolder", "Error initializing animation", e)
        }
    }

    fun onViewRecycled() {
        if (rotateAnimator != null) {
            rotateAnimator!!.end()
            rotateAnimator!!.removeAllListeners()
            rotateAnimator!!.cancel()
            rotateAnimator = null
        }
        noteStatusImg.clearAnimation()
        noteStatusImg.animate().cancel()
        noteStatusImg.rotation = 0f
    }
}
