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
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteStatus
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import java.util.function.Consumer

class GridNoteCardHolder(
    private val smartNoteGridAdapter: SmartNoteGridAdapter, itemView: View,
    private val parentActivity: ComponentActivity,
    private val handwrittenNoteRepository: HandwrittenNoteRepository,
    private val textNotesDao: TextNotesDao,
    private val smartNotebookRepository: SmartNotebookRepository,
    private val noteRelationRepository: NoteRelationRepository
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private val noteImage: ImageView = itemView.findViewById<ImageView>(R.id.card_image)
    private val textPreview: TextView = itemView.findViewById<TextView>(R.id.note_text_preview)
    private val noteTitle: TextView = itemView.findViewById<TextView>(R.id.card_name)
    private val deleteBtn: ImageButton = itemView.findViewById<ImageButton>(R.id.btn_dlt_note)
    private val noteStatusImg: ImageView = itemView.findViewById<ImageView>(R.id.img_note_status)
    private val relationViewBtn: ImageView = itemView.findViewById<ImageView>(R.id.btn_relation_view)
    private var rotateAnimation: Animation?

    private var smartNotebook: SmartNotebook? = null

    private var rotateAnimator: ObjectAnimator? = null

    init {
        rotateAnimation = AnimationUtils.loadAnimation(parentActivity, R.anim.anim_rotate)

        noteImage.setOnClickListener { onClick(itemView) }
        textPreview.setOnClickListener { onClick(itemView) }
        deleteBtn.setOnClickListener { onClickDelete() }
        relationViewBtn.setVisibility(View.GONE)

        initializeAnimation()
    }

    fun setNote(smartNotebook: SmartNotebook) {
        this.smartNotebook = smartNotebook
        val book = smartNotebook.smartBook
        val noteTitle = book.title?.trim() ?: msToDateTime(book.lastModifiedTimeMillis)
        this.noteTitle.text = noteTitle

        val notes = smartNotebook.atomicNotes
        if (notes.isEmpty()) return
        val firstNote: AtomicNoteEntity = notes[0]

        if (firstNote.noteType == NoteType.TEXT_NOTE.name) {
            BackgroundOps.execute(
                { textNotesDao.getTextNoteForNote(firstNote.noteId) },
                { textNote ->
                    noteImage.visibility = View.GONE
                    textPreview.visibility = View.VISIBLE
                    if (textNote != null) {
                        textPreview.text = textNote.noteText
                    }
                }
            )
        } else {
            BackgroundOps.execute(
                { handwrittenNoteRepository.getNoteImage(firstNote, BitmapScale.THUMBNAIL) },
                { handwrittenNoteWithImage ->
                    handwrittenNoteWithImage?.noteImage?.let { bm: Bitmap ->
                        noteImage.setImageBitmap(bm)
                    }
                }
            )
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
            relationViewBtn.setOnClickListener {
                openRelatedNotesIntent(
                    parentActivity,
                    smartNotebook!!.smartBook.bookId
                )
            }
        }
        return getAdapterPosition()
    }

    private fun onClickDelete() {
        // Show confirmation dialog
        AlertDialog.Builder(parentActivity)
            .setTitle("Delete Notebook")
            .setMessage("Are you sure you want to delete this notebook? This action cannot be undone.")
            .setPositiveButton(
                "Delete"
            ) { _: DialogInterface?, _: Int -> deleteNotebook() }
            .setNegativeButton("Cancel") { dialog: DialogInterface?, _: Int ->
                // User cancelled, do nothing
                dialog!!.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteNotebook() {
        val pos = adapterPosition
        BackgroundOps.execute(
            {
                smartNotebook!!.atomicNotes.forEach(Consumer { note: AtomicNoteEntity? ->
                    handwrittenNoteRepository.deleteHandwrittenNote(note!!)
                    noteRelationRepository.deleteNoteRelationData(note)
                })
                smartNotebookRepository.deleteSmartNotebook(smartNotebook!!)
            },
            { smartNoteGridAdapter.removeSmartNotebook(pos) }
        )
    }

    override fun onClick(v: View?) {
        openNotebookIntent(
            parentActivity,
            parentActivity.filesDir.path,
            smartNotebook!!.smartBook.bookId
        )
    }

    private fun initializeAnimation() {
        try {
            rotateAnimation = AnimationUtils.loadAnimation(parentActivity, R.anim.anim_rotate)
            if (rotateAnimation == null) {
                Log.e("GridNoteCardHolder", "Failed to load rotation animation")
                return
            }
            rotateAnimation!!.setRepeatCount(Animation.INFINITE)
            rotateAnimation!!.interpolator = LinearInterpolator()
            Log.d("GridNoteCardHolder", "Animation initialized successfully")
        } catch (e: Exception) {
            Log.e("GridNoteCardHolder", "Error initializing animation", e)
        }
    }

    fun onViewRecycled() {
        stopRotationAnimation()
    }
}
