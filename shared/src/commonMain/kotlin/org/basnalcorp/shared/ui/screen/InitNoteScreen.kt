package org.basnalcorp.shared.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOf
import org.basnalcorp.shared.state.NoteDetailStateHolder
import org.basnalcorp.shared.state.SmartNotebookStateHolder
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCommandResult
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCore
import org.basnalcorp.shared.systems.markdownnote.MarkdownNoteSystem
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.component.DesignCard
import org.basnalcorp.shared.ui.nav.Route
import org.basnalcorp.shared.ui.theme.DesignSpacing
import androidx.compose.runtime.rememberCoroutineScope

// Design tokens from pagespec/new_note_page_spec.json (NotebookDetailScreen)
private object NewNotePageTokens {
    val background = Color(0xFFEDE6D6)
    val surface = Color(0xFFF4EFE4)
    val primary = Color(0xFF4E8F5B)
    val primaryDark = Color(0xFF3E7A4C)
    val textPrimary = Color(0xFF1E1B1B)
    val textSecondary = Color(0xFF6F6A63)
    val white = Color(0xFFFFFFFF)

    val radiusCard = 24.dp
    val radiusPill = 999.dp
    val radiusButton = 20.dp

    val paddingHorizontal = 20.dp
    val paddingTop = 16.dp
    val spacing = 24.dp
    val topBarPadding = 8.dp
    val topBarSpacing = 8.dp
    val metadataSpacing = 8.dp
    val footerPaddingTop = 32.dp

    val editablePillHeight = 36.dp
    val editablePillPaddingH = 16.dp
    val topBarCornerRadius = 32.dp
    val primaryCardHeight = 120.dp
    val secondaryCardHeight = 90.dp
    val cardPadding = 20.dp
    val circularIconSize = 40.dp
    val iconSize = 24.dp

    // elevation soft: blur 20, y 8, opacity 0.08
    val shadowElevation = 8.dp
    val shadowBlur = 20.dp

    val titleLargeSize = 22.sp
    val titleMediumSize = 18.sp
    val bodySize = 14.sp
    val captionSize = 12.sp
}

/**
 * Init note screen per pagespec/new_note_page_spec.json (NotebookDetailScreen).
 * When [chronicleNotebookId] is set, screen is Chronicle-backed: pill is notebook id, rename on blur; note type buttons disabled.
 * Otherwise legacy SmartNotebook flow (create with first note).
 */
@Composable
fun InitNoteScreen(
    context: LayoutContext,
    workingPath: String,
    chronicleNotebookId: String? = null,
    stateHolder: SmartNotebookStateHolder? = null,
    noteDetailStateHolder: NoteDetailStateHolder? = null,
    chronicleCore: ChronicleCore? = null,
    markdownNoteSystem: MarkdownNoteSystem? = null,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit,
    onShowToast: ((String) -> Unit)? = null
) {
    val t = NewNotePageTokens
    val scope = rememberCoroutineScope()
    val isChronicleMode = chronicleNotebookId != null && chronicleCore != null
    val canAddMarkdownNote = isChronicleMode && markdownNoteSystem != null

    var notebookTitle by remember(chronicleNotebookId) {
        mutableStateOf(chronicleNotebookId?.takeIf { it.isNotBlank() } ?: "")
    }
    var lastCommittedChronicleId by remember(chronicleNotebookId) { mutableStateOf(chronicleNotebookId ?: "") }
    var selectedNoteType by remember { mutableStateOf<String?>(null) }
    /** When non-null, lower section shows text editor for this (bookId, noteId) instead of cards. */
    var textEditorNote by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    val notebookFlow = if (isChronicleMode) flowOf(null) else (stateHolder?.notebook ?: flowOf(null))
    val notebook by notebookFlow.collectAsState(initial = null)
    var createRequested by remember { mutableStateOf(false) }

    fun commitChronicleRename() {
        if (!isChronicleMode || chronicleCore == null) return
        val newId = notebookTitle.trim()
        if (newId.isBlank() || newId == lastCommittedChronicleId) return
        scope.launch {
            when (val r = chronicleCore.renameNotebook(lastCommittedChronicleId, newId)) {
                is ChronicleCommandResult.Success -> {
                    lastCommittedChronicleId = newId
                    onShowToast?.invoke("Renamed to $newId")
                }
                is ChronicleCommandResult.Failure ->
                    onShowToast?.invoke("Rename failed: ${r.message}")
                is ChronicleCommandResult.FailButRetry ->
                    onShowToast?.invoke("Rename retry: ${r.message}")
            }
        }
    }

    LaunchedEffect(notebook, createRequested) {
        if (isChronicleMode) return@LaunchedEffect
        if (!createRequested) return@LaunchedEffect
        val nb = notebook ?: return@LaunchedEffect
        val first = nb.atomicNotes.firstOrNull() ?: return@LaunchedEffect
        createRequested = false
        when (first.noteType) {
            "text_note" -> {
                textEditorNote = nb.smartBook.bookId to first.noteId
            }
            else -> {
                onBack()
                onNavigate(Route.SmartNotebook(bookId = nb.smartBook.bookId))
                onNavigate(
                    Route.NoteDetail(
                        bookId = nb.smartBook.bookId,
                        noteId = first.noteId,
                        isHandwritten = first.noteType == "handwritten_png"
                    )
                )
            }
        }
    }

    Scaffold(
        containerColor = t.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = t.paddingHorizontal)
                .padding(top = t.paddingTop),
            verticalArrangement = Arrangement.spacedBy(t.spacing)
        ) {
            TopControlBar(
                notebookTitle = notebookTitle,
                onNotebookTitleChange = { notebookTitle = it },
                selectedNoteType = selectedNoteType,
                onBack = onBack,
                onAddClick = {
                    if (isChronicleMode) return@TopControlBar
                    val type = selectedNoteType ?: return@TopControlBar
                    createRequested = true
                    stateHolder?.createNotebookWithFirstNote(
                        notebookTitle.trim(),
                        workingPath,
                        type
                    )
                },
                isChronicleMode = isChronicleMode,
                onPillCommit = ::commitChronicleRename
            )

            if (textEditorNote != null && !isChronicleMode) {
                val (bookId, noteId) = textEditorNote!!
                InitNoteTextEditorSection(
                    bookId = bookId,
                    noteId = noteId,
                    noteDetailStateHolder = noteDetailStateHolder,
                    onShowToast = onShowToast,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else if (!isChronicleMode) {
                MetadataSection()

                PrimaryNoteCard(
                    text = "Handwritten Note",
                    selected = selectedNoteType == "handwritten_png",
                    onClick = {
                        selectedNoteType = "handwritten_png"
                        createRequested = true
                        stateHolder?.createNotebookWithFirstNote(
                            notebookTitle.trim(),
                            workingPath,
                            "handwritten_png"
                        )
                    }
                )

                SecondaryNoteCard(
                    text = "New Text Note",
                    selected = selectedNoteType == "text_note",
                    onClick = {
                        selectedNoteType = "text_note"
                        createRequested = true
                        stateHolder?.createNotebookWithFirstNote(
                            notebookTitle.trim(),
                            workingPath,
                            "text_note"
                        )
                    }
                )

                FooterMeta()
            } else if (isChronicleMode) {
                MetadataSection()
                SecondaryNoteCard(
                    text = "New Markdown Note",
                    selected = false,
                    onClick = {
                        if (!canAddMarkdownNote) return@SecondaryNoteCard
                        val notebookId = chronicleNotebookId!!.trim()
                        if (notebookId.isBlank()) {
                            onShowToast?.invoke("Notebook name is blank")
                            return@SecondaryNoteCard
                        }
                        scope.launch {
                            when (val r = markdownNoteSystem!!.createNote(notebookId, "Untitled", "", "markdown")) {
                                is ChronicleCommandResult.Success ->
                                    onNavigate(Route.ChronicleNoteDetail(notebookId = notebookId, noteId = r.value.noteId))
                                is ChronicleCommandResult.Failure ->
                                    onShowToast?.invoke("Create failed: ${r.message}")
                                is ChronicleCommandResult.FailButRetry ->
                                    onShowToast?.invoke("Retry: ${r.message}")
                            }
                        }
                    }
                )
                Text(
                    text = "Note types coming soon. You can rename the notebook above.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = t.textSecondary,
                    fontSize = t.captionSize,
                    modifier = Modifier.padding(vertical = t.footerPaddingTop)
                )
            }
        }
    }
}

@Composable
private fun InitNoteTextEditorSection(
    bookId: Long,
    noteId: Long,
    noteDetailStateHolder: NoteDetailStateHolder?,
    onShowToast: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val t = NewNotePageTokens
    LaunchedEffect(bookId, noteId) {
        noteDetailStateHolder?.load(bookId, noteId)
    }
    val textContent by noteDetailStateHolder?.textContent?.collectAsState(initial = "") ?: remember { mutableStateOf("") }
    var localText by remember(textContent) { mutableStateOf(textContent) }
    var userHasEdited by remember { mutableStateOf(false) }
    LaunchedEffect(textContent) { localText = textContent }

    LaunchedEffect(localText) {
        if (!userHasEdited) return@LaunchedEffect
        delay(500)
        noteDetailStateHolder?.saveText(localText)
    }

    DisposableEffect(bookId, noteId) {
        onDispose {
            if (userHasEdited) noteDetailStateHolder?.saveText(localText)
        }
    }

    Column(modifier = modifier) {
        DesignCard(
            modifier = Modifier.fillMaxSize(),
            onClick = null
        ) {
            BasicTextField(
                value = localText,
                onValueChange = {
                    localText = it
                    userHasEdited = true
                    noteDetailStateHolder?.updateTextContentLocally(it)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = t.textPrimary,
                    fontSize = t.bodySize
                ),
                cursorBrush = SolidColor(t.primary),
                singleLine = false
            )
        }
    }
}

@Composable
private fun TopControlBar(
    notebookTitle: String,
    onNotebookTitleChange: (String) -> Unit,
    selectedNoteType: String?,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    isChronicleMode: Boolean = false,
    onPillCommit: (() -> Unit)? = null
) {
    val t = NewNotePageTokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(t.topBarCornerRadius))
            .background(t.surface)
            .border(1.dp, t.primaryDark.copy(alpha = 0.3f), RoundedCornerShape(t.topBarCornerRadius))
            .padding(t.topBarPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(t.topBarSpacing)
    ) {
        NewNoteIconButton(icon = "←", backgroundColor = t.white, iconColor = t.textPrimary, onClick = onBack, outlineColor = t.textSecondary.copy(alpha = 0.5f))
        EditablePill(
            value = notebookTitle,
            onValueChange = onNotebookTitleChange,
            placeholder = "Notebook name",
            modifier = Modifier.weight(1f),
            onCommit = onPillCommit
        )
        NewNoteIconButton(icon = "‹", backgroundColor = t.primaryDark, iconColor = t.white, onClick = { }, enabled = false)
        NewNoteIconButton(
            icon = "+",
            backgroundColor = t.primaryDark,
            iconColor = t.white,
            onClick = onAddClick,
            enabled = !isChronicleMode && selectedNoteType != null,
            alwaysPrimary = true
        )
        NewNoteIconButton(icon = "›", backgroundColor = t.primaryDark, iconColor = t.white, onClick = { }, enabled = false)
    }
}

@Composable
private fun NewNoteIconButton(
    icon: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    outlineColor: Color? = null,
    alwaysPrimary: Boolean = false
) {
    val t = NewNotePageTokens
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.92f else 1f
    val bg = if (alwaysPrimary || enabled) backgroundColor else t.textSecondary.copy(alpha = 0.4f)
    val contentColor = if (alwaysPrimary || enabled) iconColor else t.white

    Box(
        modifier = Modifier
            .size(t.circularIconSize)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .then(
                if (outlineColor != null) Modifier.border(1.dp, outlineColor, CircleShape)
                else Modifier
            )
            .background(bg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor
        )
    }
}

@Composable
private fun EditablePill(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onCommit: (() -> Unit)? = null
) {
    val t = NewNotePageTokens
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(t.editablePillHeight)
            .onFocusChanged { if (!it.isFocused) onCommit?.invoke() }
            .clip(RoundedCornerShape(t.radiusPill))
            .background(t.white)
            .padding(horizontal = t.editablePillPaddingH),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = t.textPrimary, fontSize = t.bodySize),
        cursorBrush = SolidColor(t.primary),
        decorationBox = { inner ->
            Box(Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = t.textSecondary,
                        fontSize = t.bodySize
                    )
                }
                inner()
            }
        }
    )
}

@Composable
private fun MetadataSection() {
    val t = NewNotePageTokens
    Column(verticalArrangement = Arrangement.spacedBy(t.metadataSpacing)) {
        Text(
            "New note",
            style = MaterialTheme.typography.titleMedium,
            color = t.textPrimary,
            fontSize = t.titleMediumSize
        )
        Text(
            "Choose a note type below, then tap + to create.",
            style = MaterialTheme.typography.bodyMedium,
            color = t.textPrimary,
            fontSize = t.bodySize
        )
        Text(
            "You can rename the notebook in the bar above.",
            style = MaterialTheme.typography.bodySmall,
            color = t.textSecondary,
            fontSize = t.captionSize
        )
    }
}

@Composable
private fun PrimaryNoteCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val t = NewNotePageTokens
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.97f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(t.primaryCardHeight)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(t.shadowElevation, RoundedCornerShape(t.radiusCard))
            .clip(RoundedCornerShape(t.radiusCard))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(t.radiusCard),
        colors = CardDefaults.cardColors(
            containerColor = t.primary,
            contentColor = t.white
        ),
        elevation = CardDefaults.cardElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(t.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                color = t.white,
                fontSize = t.titleLargeSize
            )
            Box(
                modifier = Modifier
                    .size(t.circularIconSize)
                    .clip(CircleShape)
                    .background(t.white.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text("✎", style = MaterialTheme.typography.titleLarge, color = t.white)
            }
        }
    }
}

@Composable
private fun SecondaryNoteCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val t = NewNotePageTokens
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.97f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(t.secondaryCardHeight)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(t.shadowElevation, RoundedCornerShape(t.radiusCard))
            .clip(RoundedCornerShape(t.radiusCard))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(t.radiusCard),
        colors = CardDefaults.cardColors(
            containerColor = t.surface,
            contentColor = t.textPrimary
        ),
        elevation = CardDefaults.cardElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(t.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(t.circularIconSize)
                    .clip(CircleShape)
                    .background(t.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("✎", style = MaterialTheme.typography.titleMedium, color = t.white)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = t.textPrimary,
                fontSize = t.titleMediumSize
            )
        }
    }
}

@Composable
private fun FooterMeta() {
    val t = NewNotePageTokens
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = t.footerPaddingTop),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            "Create your first note to get started.",
            style = MaterialTheme.typography.bodySmall,
            color = t.textSecondary,
            fontSize = t.captionSize
        )
    }
}
