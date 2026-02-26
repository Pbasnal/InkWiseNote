package com.originb.inkwisenote2.compose

import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.config.ConfigReader
import com.originb.inkwisenote2.modules.handwrittennotes.PageBackgroundType
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate
import com.originb.inkwisenote2.modules.handwrittennotes.ui.DrawingView
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntitiesDao
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.basnalcorp.shared.domain.AtomicNote
import org.koin.java.KoinJavaComponent.get

/**
 * Platform-specific handwritten note content for Android: syncs shared [AtomicNote] to Room,
 * embeds legacy [DrawingView] via [AndroidView], loads strokes and page template on appear,
 * saves bitmap/strokes/template on dispose.
 */
@Composable
fun HandwrittenNoteContentAndroid(
    modifier: Modifier,
    note: AtomicNote,
    bookId: Long
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val handwrittenNoteRepository = remember { get<HandwrittenNoteRepository>(HandwrittenNoteRepository::class.java) }
    val atomicNoteEntitiesDao = remember { get<AtomicNoteEntitiesDao>(AtomicNoteEntitiesDao::class.java) }

    val entity = remember(note.noteId) {
        AtomicNoteEntity().apply {
            noteId = note.noteId
            filename = note.filename
            filepath = note.filepath
            noteType = note.noteType
            pageTemplateId = note.pageTemplateId
            createdTimeMillis = note.createdTimeMillis
            lastModifiedTimeMillis = note.lastModifiedTimeMillis
        }
    }

    LaunchedEffect(note.noteId) {
        atomicNoteEntitiesDao.insertAtomicNote(entity)
    }

    var drawingViewRef by remember { mutableStateOf<DrawingView?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val root = LayoutInflater.from(ctx).inflate(R.layout.note_drawing_fragment, null, false)
            val drawingView = root.findViewById<DrawingView>(R.id.smart_drawing_view)
            drawingViewRef = drawingView

            root.findViewById<ImageButton>(R.id.eraser_button).setOnClickListener {
                drawingView.setEraserMode(true)
            }
            root.findViewById<ImageButton>(R.id.pencil_button).setOnClickListener {
                drawingView.setEraserMode(false)
            }
            root.findViewById<ImageButton>(R.id.delete_note).setOnClickListener { }
            root.findViewById<ImageButton>(R.id.debug_button).setOnClickListener { }

            root
        },
        update = { root ->
            drawingViewRef = root.findViewById<DrawingView>(R.id.smart_drawing_view)
        }
    )

    LaunchedEffect(drawingViewRef, note.noteId) {
        val view = drawingViewRef ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val strokes = handwrittenNoteRepository.readHandwrittenNoteMarkdown(entity)
            var pageTemplate: PageTemplate? = null
            handwrittenNoteRepository.getPageTemplate(entity).takeIf { it.isPresent }?.let {
                pageTemplate = it.get()
            }
            if (pageTemplate == null) {
                pageTemplate = ConfigReader.fromContext(context).getAppConfig()
                    .getPageTemplates()
                    ?.get(PageBackgroundType.BASIC_RULED_PAGE_TEMPLATE.name)
                pageTemplate?.let { handwrittenNoteRepository.saveHandwrittenNotePageTemplate(entity, it) }
            }
            withContext(Dispatchers.Main) {
                if (strokes.isNotEmpty()) view.setStrokes(strokes)
                pageTemplate?.let { view.pageTemplate = it }
            }
        }
    }

    DisposableEffect(bookId, note.noteId) {
        onDispose {
            val view = drawingViewRef ?: return@onDispose
            val bitmap = view.bitmap
            val strokes = view.strokes
            val pageTemplate = view.pageTemplate
            scope.launch(Dispatchers.IO) {
                handwrittenNoteRepository.saveHandwrittenNotes(
                    bookId = bookId,
                    atomicNote = entity,
                    bitmap = bitmap,
                    pageTemplate = pageTemplate,
                    strokes = strokes,
                    context = context
                )
            }
        }
    }
}
