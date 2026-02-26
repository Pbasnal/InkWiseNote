package org.basnalcorp.shared.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import org.basnalcorp.shared.domain.AtomicNote
import org.basnalcorp.shared.state.NoteDetailStateHolder
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.WindowSizeClass
import org.basnalcorp.shared.ui.component.DesignCard
import org.basnalcorp.shared.ui.component.DesignTopAppBar
import org.basnalcorp.shared.ui.component.SecondaryButton
import org.basnalcorp.shared.ui.theme.DesignColors
import org.basnalcorp.shared.ui.theme.DesignSpacing

/**
 * Optional platform-specific content for handwritten notes (e.g. Android embeds DrawingView).
 * When null, a placeholder is shown. When non-null and note type is handwritten_png, this is invoked.
 */
@Composable
fun NoteDetailScreen(
    context: LayoutContext,
    stateHolder: NoteDetailStateHolder?,
    bookId: Long,
    noteId: Long,
    isHandwritten: Boolean,
    onNavigate: (org.basnalcorp.shared.ui.nav.Route) -> Unit,
    onBack: () -> Unit,
    onShowToast: ((String) -> Unit)? = null,
    handwrittenContent: (@Composable (Modifier, AtomicNote, Long) -> Unit)? = null
) {
    LaunchedEffect(bookId, noteId) {
        stateHolder?.load(bookId, noteId)
    }
    val note by stateHolder?.note?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }
    val textContent by stateHolder?.textContent?.collectAsState(initial = "") ?: remember { mutableStateOf("") }
    val isInitMode = stateHolder?.isInitMode == true

    when (context.windowSizeClass) {
        WindowSizeClass.Compact -> NoteDetailLayout(
            note = note,
            textContent = textContent,
            isInitMode = isInitMode,
            isHandwritten = isHandwritten,
            stateHolder = stateHolder,
            bookId = bookId,
            onBack = onBack,
            onShowToast = onShowToast,
            handwrittenContent = handwrittenContent
        )
        WindowSizeClass.Medium,
        WindowSizeClass.Expanded -> NoteDetailLayout(
            note = note,
            textContent = textContent,
            isInitMode = isInitMode,
            isHandwritten = isHandwritten,
            stateHolder = stateHolder,
            bookId = bookId,
            onBack = onBack,
            onShowToast = onShowToast,
            handwrittenContent = handwrittenContent
        )
    }
}

@Composable
private fun NoteDetailLayout(
    note: org.basnalcorp.shared.domain.AtomicNote?,
    textContent: String,
    isInitMode: Boolean,
    isHandwritten: Boolean,
    stateHolder: NoteDetailStateHolder?,
    bookId: Long,
    onBack: () -> Unit,
    onShowToast: ((String) -> Unit)? = null,
    handwrittenContent: (@Composable (Modifier, AtomicNote, Long) -> Unit)? = null
) {
    var localText by remember(textContent) { mutableStateOf(textContent) }
    var userHasEdited by remember { mutableStateOf(false) }
    LaunchedEffect(textContent) { localText = textContent }
    val title = when {
        note == null -> "Note"
        isInitMode -> "New note"
        else -> when (note.noteType) {
            "text_note" -> "Text note"
            "handwritten_png" -> "Handwritten note"
            else -> "Note"
        }
    }

    Scaffold(
        topBar = {
            DesignTopAppBar(
                title = title,
                navigationIcon = { IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.bodyLarge) } },
                actions = { }
            )
        }
    ) { padding ->
        if (note == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Loading…", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (isInitMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(DesignSpacing.layoutPaddingMobile),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.sectionSpacing)
            ) {
                Text(
                    "Choose note type",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                DesignCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        stateHolder?.setNoteTypeToText(bookId)
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Text note", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Write with keyboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                DesignCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        stateHolder?.setNoteTypeToHandwritten()
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Handwritten note", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Draw with pen or finger", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            val currentNote = requireNotNull(note)
            when (currentNote.noteType) {
            "text_note" -> {
                LaunchedEffect(localText) {
                    if (!userHasEdited) return@LaunchedEffect
                    kotlinx.coroutines.delay(500)
                    stateHolder?.saveText(localText)
                }
                DisposableEffect(bookId, currentNote.noteId) {
                    onDispose {
                        if (userHasEdited) stateHolder?.saveText(localText)
                    }
                }
                // Full-page text input in a card so the editable area is clearly visible.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(DesignSpacing.layoutPaddingMobile)
                ) {
                    DesignCard(
                        modifier = Modifier.fillMaxSize(),
                        onClick = null
                    ) {
                        BasicTextField(
                            value = localText,
                            onValueChange = {
                                localText = it
                                userHasEdited = true
                                stateHolder?.updateTextContentLocally(it)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            singleLine = false
                        )
                    }
                }
            }
            "handwritten_png" -> {
                if (handwrittenContent != null) {
                    handwrittenContent(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(DesignSpacing.layoutPaddingMobile),
                        currentNote,
                        bookId
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(DesignSpacing.layoutPaddingMobile),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(DesignSpacing.sectionSpacing)) {
                            Text(
                                "Handwritten note",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Drawing is available on Android.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(DesignSpacing.scale16)) {
                                SecondaryButton(text = "Clear", onClick = { })
                                SecondaryButton(text = "Undo", onClick = { })
                            }
                        }
                    }
                }
            }
            else -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Unknown note type", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            }
        }
    }
}
