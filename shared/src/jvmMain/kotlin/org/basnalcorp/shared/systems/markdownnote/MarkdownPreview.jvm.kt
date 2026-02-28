package org.basnalcorp.shared.systems.markdownnote

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.m3.Markdown

@Composable
actual fun MarkdownPreviewContent(
    content: String,
    modifier: Modifier
) {
    Markdown(
        content = content,
        modifier = modifier.verticalScroll(rememberScrollState())
    )
}
