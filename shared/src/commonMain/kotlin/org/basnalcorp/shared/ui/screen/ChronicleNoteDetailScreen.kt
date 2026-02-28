package org.basnalcorp.shared.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCommandResult
import org.basnalcorp.shared.systems.markdownnote.MarkdownBody
import org.basnalcorp.shared.systems.markdownnote.MarkdownNoteSystem
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.component.DesignTopAppBar
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.theme.DesignColors
import org.basnalcorp.shared.ui.theme.DesignComponents
import org.basnalcorp.shared.debug.reportComposerDebug

/**
 * Chronicle markdown note editor: loads note via [MarkdownNoteSystem.getNote],
 * edits title and body, saves via [MarkdownNoteSystem.updateNote] with expectedLastModified.
 * Toggle between Raw (editable source) and Preview (rendered body) via the top bar.
 */
private enum class ViewMode { Raw, Preview }

@Composable
fun ChronicleNoteDetailScreen(
    context: LayoutContext,
    notebookId: String,
    noteId: Long,
    markdownNoteSystem: MarkdownNoteSystem?,
    onBack: () -> Unit,
    onNavigate: (Route) -> Unit,
    onShowToast: ((String) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    var viewMode by remember { mutableStateOf(ViewMode.Raw) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var expectedLastModified by remember { mutableStateOf(0L) }
    var loaded by remember { mutableStateOf(false) }

    // #region agent log
    LaunchedEffect(Unit) { reportComposerDebug() }
    // #endregion
    LaunchedEffect(notebookId, noteId, markdownNoteSystem) {
        if (markdownNoteSystem == null) return@LaunchedEffect
        val note = markdownNoteSystem.getNote(notebookId, noteId)
        if (note != null) {
            title = note.title
            body = note.body
            expectedLastModified = note.lastModified
        }
        loaded = true
    }

    fun save() {
        if (markdownNoteSystem == null || expectedLastModified == 0L) return
        scope.launch {
            when (val r = markdownNoteSystem.updateNote(
                noteId = noteId,
                notebookId = notebookId,
                title = title,
                body = body,
                expectedLastModified = expectedLastModified,
                noteType = "markdown"
            )) {
                is ChronicleCommandResult.Success -> {
                    expectedLastModified = r.value.lastModified
                    onShowToast?.invoke("Saved")
                }
                is ChronicleCommandResult.Failure ->
                    onShowToast?.invoke("Save failed: ${r.message}")
                is ChronicleCommandResult.FailButRetry ->
                    onShowToast?.invoke("Conflict: ${r.message}. Reload and try again.")
            }
        }
    }

    Scaffold(
        topBar = {
            DesignTopAppBar(
                title = "Note",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.bodyLarge)
                    }
                },
                actions = {
                    if (viewMode == ViewMode.Raw) {
                        Text(
                            text = "Preview",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DesignColors.primaryBase,
                            modifier = Modifier
                                .padding(DesignComponents.touchTargetMin / 2)
                                .clickable { viewMode = ViewMode.Preview }
                        )
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DesignColors.primaryBase,
                            modifier = Modifier
                                .padding(DesignComponents.touchTargetMin / 2)
                                .clickable { save() }
                        )
                    } else {
                        Text(
                            text = "Raw",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DesignColors.primaryBase,
                            modifier = Modifier
                                .padding(DesignComponents.touchTargetMin / 2)
                                .clickable { viewMode = ViewMode.Raw }
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (!loaded && markdownNoteSystem != null) {
            Text(
                "Loading…",
                style = MaterialTheme.typography.bodyLarge,
                color = DesignColors.textMuted,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else if (markdownNoteSystem == null) {
            Text(
                "MarkdownNoteSystem not available",
                style = MaterialTheme.typography.bodyLarge,
                color = DesignColors.textMuted,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            when (viewMode) {
                ViewMode.Raw -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textStyle = MaterialTheme.typography.titleLarge.copy(color = DesignColors.textPrimary),
                        singleLine = true,
                        decorationBox = { inner ->
                            Column {
                                Text("Title", style = MaterialTheme.typography.labelMedium, color = DesignColors.textMuted)
                                inner()
                            }
                        }
                    )
                    BasicTextField(
                        value = body,
                        onValueChange = { body = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = DesignColors.textPrimary),
                        cursorBrush = SolidColor(DesignColors.primaryBase),
                        singleLine = false,
                        decorationBox = { inner ->
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text("Body", style = MaterialTheme.typography.labelMedium, color = DesignColors.textMuted)
                                inner()
                            }
                        }
                    )
                }
                ViewMode.Preview -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = title.ifBlank { "Untitled" },
                        style = MaterialTheme.typography.titleLarge,
                        color = DesignColors.textPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    MarkdownBody(
                        content = body,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }
}
