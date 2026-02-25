package com.originb.inkwisenote2.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "InkWiseNote"
    ) {
        // Empty window for Phase 1. Shared UI will be added in Phase 6.
    }
}
