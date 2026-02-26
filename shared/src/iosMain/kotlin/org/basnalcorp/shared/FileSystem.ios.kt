package org.basnalcorp.shared

/**
 * iOS actual: returns empty list until platform file APIs are wired.
 * File explorer is primarily used on Android/Desktop in Phase 3.
 */
actual fun listDirectory(path: String): List<FileExplorerItem> {
    return emptyList()
}

actual fun createDirectory(path: String): Boolean {
    return false
}

actual fun createNotebookDirectory(parentPath: String, title: String): String {
    val path = if (parentPath.endsWith("/")) "${parentPath}$title" else "$parentPath/$title"
    return path
}

actual fun writeTextFile(filePath: String, content: String) {
    // Stub until iOS file APIs are wired
}
