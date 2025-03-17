package com.originb.inkwisenote2.modules.handwrittennotes.ui

import android.graphics.*
import android.view.View
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.BitmapScale
import com.originb.inkwisenote2.config.ConfigReader
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.backgroundjobs.Events.NoteDeleted
import com.originb.inkwisenote2.modules.handwrittennotes.PageBackgroundType
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.ui.NoteHolder
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer

class HandwrittenNoteHolder(
    itemView: View,
    parentActivity: ComponentActivity?,
    smartNotebookRepository: SmartNotebookRepository
) : NoteHolder(itemView, parentActivity, smartNotebookRepository) {
    private val drawingView: DrawingView = itemView.findViewById(R.id.smart_drawing_view)
    private var atomicNote: AtomicNoteEntity? = null

    private val deleteNote: ImageButton = itemView.findViewById(R.id.delete_note)

    private var bookId: Long = 0

    private val handwrittenNoteRepository: HandwrittenNoteRepository
    private val configReader: ConfigReader?

    init {
        deleteNote.setOnClickListener { v: View? ->
            BackgroundOps.Companion.execute(
                Runnable {
                    EventBus.getDefault().post(
                        NoteDeleted(
                            smartNotebookRepository.getSmartNotebooks(bookId).get(),
                            atomicNote
                        )
                    )
                })
        }

        handwrittenNoteRepository = Repositories.Companion.getInstance().getHandwrittenNoteRepository()
        configReader = ConfigReader.Companion.getInstance()
    }

    override fun setNote(bookId: Long, atomicNote: AtomicNoteEntity) {
        this.bookId = bookId
        this.atomicNote = atomicNote
        BackgroundOps.Companion.execute<Optional<Bitmap>?>(
            Callable<Optional<Bitmap?>?> {
                handwrittenNoteRepository.getNoteImage(
                    atomicNote,
                    BitmapScale.FULL_SIZE
                ).noteImage
            },
            Consumer<Optional<Bitmap>?> { bitmapOpt: Optional<Bitmap>? ->
                if (bitmapOpt!!.isPresent) {
                    bitmapOpt.ifPresent { bitmap: Bitmap -> drawingView.bitmap = bitmap }
                    return@execute
                }
                val newBitmap = if (useDefaultBitmap()) {
                    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                } else {
                    Bitmap.createBitmap(drawingView.currentWidth, drawingView.currentHeight, Bitmap.Config.ARGB_8888)
                }
                BackgroundOps.Companion.execute(Runnable {
                    handwrittenNoteRepository.saveHandwrittenNoteImage(
                        atomicNote,
                        newBitmap
                    )
                })
                drawingView.bitmap = newBitmap
            })

        BackgroundOps.Companion.execute<Optional<PageTemplate>?>(
            Callable<Optional<PageTemplate?>?> { handwrittenNoteRepository.getPageTemplate(atomicNote) },
            Consumer<Optional<PageTemplate?>?> { pageTemplateOpt: Optional<PageTemplate?>? ->
                if (pageTemplateOpt!!.isPresent) {
                    pageTemplateOpt.ifPresent { data: PageTemplate? -> drawingView.pageTemplate = data }
                    return@execute
                }
                val pageTemplate: PageTemplate =
                    configReader.getAppConfig().pageTemplates[PageBackgroundType.BASIC_RULED_PAGE_TEMPLATE.name]
                BackgroundOps.Companion.execute(Runnable {
                    handwrittenNoteRepository.saveHandwrittenNotePageTemplate(
                        atomicNote,
                        pageTemplate
                    )
                })
                drawingView.pageTemplate = pageTemplate
            })
    }

    override fun saveNote(): Boolean {
        // Todo: need to reload note images on home page once this is done

        return handwrittenNoteRepository.saveHandwrittenNotes(
            bookId,
            atomicNote,
            drawingView.bitmap,
            drawingView.pageTemplate
        )
    }

    fun useDefaultBitmap(): Boolean {
        return drawingView.currentWidth * drawingView.currentHeight == 0
    }
}
