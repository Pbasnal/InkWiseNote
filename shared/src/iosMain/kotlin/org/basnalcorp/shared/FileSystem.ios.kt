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

actual fun readTextFile(filePath: String): String {
    // Stub until iOS file APIs are wired
    return ""
}

actual fun deleteFile(filePath: String): Boolean {
    // Stub until iOS file APIs are wired
    return false
}

actual fun deleteDirectory(path: String): Boolean {
    // Stub until iOS file APIs are wired
    return false
}

actual fun renameDirectory(oldPath: String, newPath: String): Boolean {
    // Stub until iOS file APIs are wired
    return false
}

actual fun pathExists(path: String): Boolean {
    return false
}
