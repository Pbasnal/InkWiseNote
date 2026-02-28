package org.basnalcorp.shared.systems.markdownnote

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders markdown body in Preview mode. Android actual uses plain text to avoid
 * Compose runtime version conflict (NoSuchMethodError Composer.shouldExecute).
 * JVM and iOS use mikepenz Markdown composable.
 */
@Composable
expect fun MarkdownPreviewContent(
    content: String,
    modifier: Modifier = Modifier
)
