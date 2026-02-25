package com.originb.inkwisenote2.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.basnalcorp.shared.platform

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "InkWiseNote (${platform()})",
        state = rememberWindowState()
    ) {
        MaterialTheme {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("InkWiseNote on ${platform()}")
    }
}
