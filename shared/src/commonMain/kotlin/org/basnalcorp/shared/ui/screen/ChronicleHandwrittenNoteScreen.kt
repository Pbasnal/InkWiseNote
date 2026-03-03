package org.basnalcorp.shared.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCommandResult
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCore
import org.basnalcorp.shared.systems.chroniclecore.ChronicleFileSystem
import org.basnalcorp.shared.systems.handwritten.DrawingSurface
import org.basnalcorp.shared.systems.handwritten.DrawingTool
import org.basnalcorp.shared.systems.handwritten.HandwrittenConfig
import org.basnalcorp.shared.systems.handwritten.HandwrittenNoteSaved
import org.basnalcorp.shared.systems.handwritten.PenConfig
import org.basnalcorp.shared.systems.handwritten.parseStrokesBlock
import org.basnalcorp.shared.systems.handwritten.setStrokesBlockInBody
import org.basnalcorp.shared.systems.handwritten.Stroke
import org.basnalcorp.shared.systems.handwritten.StrokeStorage
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.component.DesignTopAppBar
import org.basnalcorp.shared.ui.nav.Route

private const val AUTOSAVE_DEBOUNCE_MS = 500L
private const val STROKES_FILENAME_SUFFIX = ".strokes.json"

@Composable
fun ChronicleHandwrittenNoteScreen(
    context: LayoutContext,
    notebookId: String,
    noteId: Long,
    chronicleCore: ChronicleCore?,
    chronicleFileSystem: ChronicleFileSystem?,
    notesRoot: String?,
    onBack: () -> Unit,
    onNavigate: (Route) -> Unit,
    onShowToast: ((String) -> Unit)? = null,
    onHandwrittenNoteSaved: ((HandwrittenNoteSaved) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var expectedLastModified by remember { mutableStateOf(0L) }
    var loaded by remember { mutableStateOf(false) }
    val strokes = remember { mutableStateListOf<Stroke>() }
    var lastSavedHash by remember { mutableStateOf("") }
    var currentTool by remember { mutableStateOf(DrawingTool.Pen) }
    val config = remember { HandwrittenConfig.default() }
    var penConfig by remember {
        mutableStateOf(PenConfig(config.defaultColor, config.defaultThickness))
    }

    val strokesFilePath = remember(notebookId, noteId, notesRoot) {
        if (notesRoot.isNullOrBlank()) null else "$notesRoot/$notebookId/$noteId$STROKES_FILENAME_SUFFIX"
    }

    LaunchedEffect(notebookId, noteId, chronicleCore) {
        if (chronicleCore == null || chronicleFileSystem == null || notesRoot.isNullOrBlank()) return@LaunchedEffect
        val note = chronicleCore.getNote(notebookId, noteId) ?: run {
            loaded = true
            return@LaunchedEffect
        }
        title = note.title
        body = note.body
        expectedLastModified = note.lastModified
        val pathFromBody = parseStrokesBlock(note.body)
        val path = strokesFilePath ?: run {
            loaded = true
            return@LaunchedEffect
        }
        if (!chronicleFileSystem.exists(path)) {
            StrokeStorage.saveStrokes(chronicleFileSystem, path, emptyList())
        }
        val list = StrokeStorage.loadStrokes(chronicleFileSystem, path)
        strokes.clear()
        strokes.addAll(list)
        lastSavedHash = StrokeStorage.strokesHash(list)
        loaded = true
    }

    fun saveStrokesAndNote(isAutosave: Boolean) {
        if (chronicleCore == null || chronicleFileSystem == null || strokesFilePath == null || expectedLastModified == 0L) return
        val newHash = StrokeStorage.strokesHash(strokes)
        if (newHash == lastSavedHash) return
        scope.launch {
            StrokeStorage.saveStrokes(chronicleFileSystem, strokesFilePath, strokes)
            val newBody = setStrokesBlockInBody(body, "$noteId$STROKES_FILENAME_SUFFIX")
            when (val r = chronicleCore.updateNote(
                noteId = noteId,
                notebookId = notebookId,
                updatedTitle = title,
                updatedBody = newBody,
                expectedLastModified = expectedLastModified,
                preserveUnknownKeys = mapOf("note_type" to "handwritten")
            )) {
                is ChronicleCommandResult.Success -> {
                    expectedLastModified = r.value.lastModified
                    body = newBody
                    lastSavedHash = newHash
                    onHandwrittenNoteSaved?.invoke(HandwrittenNoteSaved(noteId, notebookId, isAutosave))
                    if (!isAutosave) onShowToast?.invoke("Saved")
                }
                is ChronicleCommandResult.Failure ->
                    onShowToast?.invoke("Save failed: ${r.message}")
                is ChronicleCommandResult.FailButRetry ->
                    onShowToast?.invoke("Conflict: ${r.message}")
            }
        }
    }

    LaunchedEffect(strokes.toList()) {
        if (!loaded || chronicleCore == null) return@LaunchedEffect
        delay(AUTOSAVE_DEBOUNCE_MS)
        saveStrokesAndNote(isAutosave = true)
    }

    DisposableEffect(notebookId, noteId) {
        onDispose {
            if (StrokeStorage.strokesHash(strokes) != lastSavedHash) {
                saveStrokesAndNote(isAutosave = false)
            }
        }
    }

    Scaffold(
        topBar = {
            DesignTopAppBar(
                title = title.ifBlank { "Note" },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.bodyLarge)
                    }
                },
                actions = {
                    Text(
                        text = "Pen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (currentTool == DrawingTool.Pen) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp).clickable { currentTool = DrawingTool.Pen }
                    )
                    Text(
                        text = "Eraser",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (currentTool == DrawingTool.Eraser) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp).clickable { currentTool = DrawingTool.Eraser }
                    )
                }
            )
        }
    ) { padding: PaddingValues ->
        if (!loaded || chronicleCore == null || chronicleFileSystem == null || notesRoot.isNullOrBlank()) {
            Text(
                "Loading…",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxSize().padding(padding)
            )
        } else {
            DrawingSurface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                strokes = strokes,
                onStrokesChanged = { newList ->
                    strokes.clear()
                    strokes.addAll(newList)
                },
                tool = currentTool,
                penConfig = penConfig,
                config = config
            )
        }
    }
}
