package org.basnalcorp.shared.systems.chroniclecore

/**
 * File system interface for ChronicleCore. All note file and directory operations
 * go through this interface. Implementations delegate to platform-specific file APIs.
 */
interface ChronicleFileSystem {

    /**
     * Reads the full text content of the file at [path].
     * @throws Exception if the file does not exist or cannot be read.
     */
    fun readTextFile(path: String): String

    /**
     * Writes [content] to the file at [path], creating parent directories if needed.
     * Overwrites the file if it exists.
     */
    fun writeTextFile(path: String, content: String)

    /**
     * Creates the directory at [path] and any missing parents. No-op if already exists.
     * @return true if the directory exists after the call, false on failure.
     */
    fun createDirectory(path: String): Boolean

    /**
     * Deletes the file at [path].
     * @return true if the file was deleted or did not exist, false on failure.
     */
    fun deleteFile(path: String): Boolean

    /**
     * Deletes the directory at [path] and all its contents recursively.
     * @return true if the directory was deleted or did not exist, false on failure.
     */
    fun deleteDirectory(path: String): Boolean

    /**
     * Renames/moves the directory from [oldPath] to [newPath].
     * @return true if the rename succeeded, false on failure.
     */
    fun renameDirectory(oldPath: String, newPath: String): Boolean

    /**
     * Returns true if a file or directory exists at [path], false otherwise.
     */
    fun exists(path: String): Boolean

    /**
     * Lists direct children (files and directories) at [path].
     * Returns empty list if path is not a directory or on error.
     */
    fun listDirectory(path: String): List<ChronicleFileEntry>
}

/**
 * A file or directory entry when listing a directory.
 */
data class ChronicleFileEntry(
    val path: String,
    val name: String,
    val isDirectory: Boolean
)
