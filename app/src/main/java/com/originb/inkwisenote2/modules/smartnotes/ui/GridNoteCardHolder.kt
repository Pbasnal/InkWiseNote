package com.originb.inkwisenote2.modules.smartnotes.ui

import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.BitmapScale
import com.originb.inkwisenote2.common.DateTimeUtils.msToDateTime
import com.originb.inkwisenote2.common.Routing.RelatedNotesActivity.openRelatedNotesIntent
import com.originb.inkwisenote2.common.Routing.SmartNotebookActivity.openNotebookIntent
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteStatus
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import lombok.Getter
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate

class GridNoteCardHolder(
    private val smartNoteGridAdapter: SmartNoteGridAdapter, itemView: View,
    private val parentActivity: ComponentActivity,
    handwrittenNoteRepository: HandwrittenNoteRepository,
    textNotesDao: TextNotesDao,
    smartNotebookRepository: SmartNotebookRepository,
    noteRelationRepository: NoteRelationRepository
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    @Getter
    private val itemView: View?

    private val noteImage: ImageView
    private val textPreview: TextView
    private val noteTitle: TextView
    private val deleteBtn: ImageButton
    private val noteStatusImg: ImageView
    private val relationViewBtn: ImageView
    private var rotateAnimation: Animation?

    private var smartNotebook: SmartNotebook? = null
    private val handwrittenNoteRepository: HandwrittenNoteRepository
    private val textNotesDao: TextNotesDao
    private val smartNotebookRepository: SmartNotebookRepository
    private val noteRelationRepository: NoteRelationRepository

    private var rotateAnimator: ObjectAnimator? = null

    init {
        this.itemView = itemView
        this.handwrittenNoteRepository = handwrittenNoteRepository
        this.textNotesDao = textNotesDao
        this.smartNotebookRepository = smartNotebookRepository
        this.noteRelationRepository = noteRelationRepository

        noteImage = itemView.findViewById<ImageView>(R.id.card_image)
        textPreview = itemView.findViewById<TextView>(R.id.note_text_preview)
        noteTitle = itemView.findViewById<TextView>(R.id.card_name)
        deleteBtn = itemView.findViewById<ImageButton>(R.id.btn_dlt_note)
        relationViewBtn = itemView.findViewById<ImageView>(R.id.btn_relation_view)
        noteStatusImg = itemView.findViewById<ImageView>(R.id.img_note_status)
        rotateAnimation = AnimationUtils.loadAnimation(parentActivity, R.anim.anim_rotate)

        noteImage.setOnClickListener(View.OnClickListener { view: View? -> onClick(itemView) })
        textPreview.setOnClickListener(View.OnClickListener { view: View? -> onClick(itemView) })
        deleteBtn.setOnClickListener(View.OnClickListener { view: View? -> onClickDelete() })
        relationViewBtn.setVisibility(View.GONE)

        initializeAnimation()
    }

    fun setNote(smartNotebook: SmartNotebook) {
        this.smartNotebook = smartNotebook
        val noteTitle: String? = Optional.ofNullable<T?>(smartNotebook.getSmartBook().getTitle())
            .filter(Predicate { title: T? -> !title.trim().isEmpty() })
            .orElse(msToDateTime(smartNotebook.getSmartBook().getLastModifiedTimeMillis()))
        this.noteTitle.setText(noteTitle)

        val numberOfNotes: Int = smartNotebook.getAtomicNotes().size()
        if (numberOfNotes == 0) return
        val firstNote: AtomicNoteEntity = smartNotebook.getAtomicNotes().get(0)

        if (NoteType.TEXT_NOTE.equals(firstNote.getNoteType())) {
            execute(
                Runnable { textNotesDao.getTextNoteForNote(firstNote.getNoteId()) },
                Runnable { textNote ->
                    noteImage.setVisibility(View.GONE)
                    textPreview.setVisibility(View.VISIBLE)
                    if (textNote != null) {
                        textPreview.setText(textNote.getNoteText())
                    }
                })
        } else {
            execute(
                Runnable { handwrittenNoteRepository.getNoteImage(firstNote, BitmapScale.THUMBNAIL) },
                Runnable { handwrittenNoteWithImage ->
                    handwrittenNoteWithImage.noteImage.ifPresent({ bm: Bitmap? ->
                        noteImage.setImageBitmap(
                            bm
                        )
                    })
                })
        }
    }

    fun updateNoteStatus(noteStatus: NoteStatus) {
        // Always cleanup any existing animation first
        stopRotationAnimation()

        if (TextProcessingStage.NOTE_READY != noteStatus.status) {
            // Start new animation for processing state
            noteStatusImg.setImageResource(R.drawable.ic_in_process)


            // Create rotation animation programmatically
            rotateAnimator = ObjectAnimator.ofFloat(
                noteStatusImg,
                "rotation",
                0f, 360f
            )
            rotateAnimator!!.setDuration(1000) // 1 second per rotation
            rotateAnimator!!.setRepeatCount(ObjectAnimator.INFINITE)
            rotateAnimator!!.setInterpolator(LinearInterpolator())


            // Start animation
            rotateAnimator!!.start()

            Log.d("GridNoteCardHolder", "Starting rotation animation")
        } else {
            // Set ready status image without animation
            noteStatusImg.setImageResource(R.drawable.ic_tick_circle)
            Log.d("GridNoteCardHolder", "Set to ready status")
        }
    }

    /**
     * Helper method to stop rotation animation and reset view state
     */
    private fun stopRotationAnimation() {
        Log.d("GridNoteCardHolder", "Stopping rotation animation")

        if (rotateAnimator != null) {
            try {
                rotateAnimator!!.cancel()
                rotateAnimator!!.removeAllListeners()
                rotateAnimator = null
            } catch (e: Exception) {
                Log.e("GridNoteCardHolder", "Error stopping animator", e)
            }
        }

        try {
            noteStatusImg.clearAnimation()
            noteStatusImg.animate().cancel()
            noteStatusImg.setRotation(0f) // Reset rotation
        } catch (e: Exception) {
            Log.e("GridNoteCardHolder", "Error resetting image view", e)
        }
    }

    fun updateNoteRelation(isRelated: Boolean): Int {
        if (!isRelated) {
            relationViewBtn.setVisibility(View.GONE)
        } else {
            relationViewBtn.setVisibility(View.VISIBLE)
            relationViewBtn.setOnClickListener(View.OnClickListener { v: View? ->
                openRelatedNotesIntent(
                    parentActivity,
                    smartNotebook.getSmartBook().getBookId()
                )
            })
        }
        return getAdapterPosition()
    }

    private fun onClickDelete() {
        // Show confirmation dialog
        AlertDialog.Builder(parentActivity)
            .setTitle("Delete Notebook")
            .setMessage("Are you sure you want to delete this notebook? This action cannot be undone.")
            .setPositiveButton(
                "Delete",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> deleteNotebook() })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                // User cancelled, do nothing
                dialog!!.dismiss()
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteNotebook() {
        execute(Runnable {
            smartNotebook!!.atomicNotes.forEach(Consumer { note: AtomicNoteEntity? ->
                handwrittenNoteRepository.deleteHandwrittenNote(note!!)
                noteRelationRepository.deleteNoteRelationData(note)
            })
            smartNotebookRepository.deleteSmartNotebook(smartNotebook!!)
        })

        smartNoteGridAdapter.removeSmartNotebook(getAdapterPosition())
    }

    override fun onClick(v: View?) {
//        if (smartNotebook.getAtomicNotes().get(0).getNoteType().equals(NoteType.TEXT_NOTE.toString())) {
//            Routing.TextNoteActivity.openNotebookIntent(parentActivity,
//                    parentActivity.getFilesDir().getPath(),
//                    smartNotebook.getSmartBook().getBookId());
//        } else {
        openNotebookIntent(
            parentActivity,
            parentActivity.getFilesDir().getPath(),
            smartNotebook.getSmartBook().getBookId()
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
            rotateAnimation!!.setRepeatCount(Animation.INFINITE)
            rotateAnimation!!.setInterpolator(LinearInterpolator())
            Log.d("GridNoteCardHolder", "Animation initialized successfully")
        } catch (e: Exception) {
            Log.e("GridNoteCardHolder", "Error initializing animation", e)
        }
    }

    fun onViewRecycled() {
        stopRotationAnimation()
    }
}
