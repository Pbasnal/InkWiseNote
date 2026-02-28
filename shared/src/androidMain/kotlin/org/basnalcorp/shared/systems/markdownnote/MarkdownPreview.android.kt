package org.basnalcorp.shared.systems.markdownnote

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Android: plain text only to avoid NoSuchMethodError (Composer.shouldExecute) from
 * Compose runtime / mikepenz version mismatch. Rendered markdown can be added later
 * when runtime is aligned.
 */
@Composable
actual fun MarkdownPreviewContent(
    content: String,
    modifier: Modifier
) {
    Text(
        text = content,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.verticalScroll(rememberScrollState())
    )
}
