package org.basnalcorp.shared.systems.markdownnote

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders markdown body content. All parsing and rendering logic lives in the markdown package;
 * callers only display the result. Plain text is shown when content is blank or rendering fails.
 *
 * On Android, preview uses plain text to avoid Compose runtime version conflict.
 * Optional frontmatter: In the future, frontmatter may include `rendering: commonmark` to select
 * renderer or options; [MarkdownBody] can then accept an optional hint parameter. No change to
 * storage format until then.
 */
@Composable
fun MarkdownBody(
    content: String,
    modifier: Modifier = Modifier
) {
    if (content.isBlank()) {
        PlainTextFallback(text = content, modifier = modifier)
        return
    }
    MarkdownPreviewContent(content = content, modifier = modifier)
}

@Composable
private fun PlainTextFallback(
    text: String,
    modifier: Modifier = Modifier
) {
    val displayText = if (text.isBlank()) "Empty" else text
    Text(
        text = displayText,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth()
    )
}
