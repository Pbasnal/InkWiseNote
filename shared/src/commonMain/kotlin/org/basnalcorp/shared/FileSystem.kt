package org.basnalcorp.shared

/**
 * Represents a file or directory entry for the file explorer (Phase 3).
 * Platform-agnostic; path is absolute.
 */
data class FileExplorerItem(
    val path: String,
    val name: String,
    val isDirectory: Boolean
)

/**
 * Lists direct children of the given directory path.
 * Returns empty list if path is not a directory or on error.
 * Use [appStorageRoot] for the app's root directory.
 */
expect fun listDirectory(path: String): List<FileExplorerItem>

/**
 * Creates the directory (and any missing parents). No-op if already exists.
 * Returns true if the directory exists after the call.
 */
expect fun createDirectory(path: String): Boolean

/**
 * Creates the notebook directory at [parentPath]/[title] (and any missing parents).
 * Returns the absolute path of the notebook directory (e.g. parentPath/title).
 * Use when creating a new notebook so note filepath points to this directory.
 */
expect fun createNotebookDirectory(parentPath: String, title: String): String

/**
 * Writes [content] to the file at [filePath], creating parent directories if needed.
 * Overwrites the file if it exists.
 */
expect fun writeTextFile(filePath: String, content: String)
