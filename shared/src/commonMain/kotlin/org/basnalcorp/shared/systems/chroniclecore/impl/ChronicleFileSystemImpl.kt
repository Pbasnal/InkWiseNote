package org.basnalcorp.shared.systems.chroniclecore.impl

import org.basnalcorp.shared.systems.chroniclecore.ChronicleFileEntry
import org.basnalcorp.shared.systems.chroniclecore.ChronicleFileSystem

/**
 * ChronicleCore file system implementation that delegates to provided operations.
 * Use [createChronicleFileSystem] from platform code to get an instance wired to actual file APIs.
 */
class ChronicleFileSystemImpl(
    private val readTextFile: (path: String) -> String,
    private val writeTextFile: (path: String, content: String) -> Unit,
    private val createDirectory: (path: String) -> Boolean,
    private val deleteFile: (path: String) -> Boolean,
    private val deleteDirectory: (path: String) -> Boolean,
    private val renameDirectoryFn: (oldPath: String, newPath: String) -> Boolean,
    private val existsPath: (path: String) -> Boolean,
    private val listDirectory: (path: String) -> List<ChronicleFileEntry>
) : ChronicleFileSystem {

    override fun readTextFile(path: String): String =
        readTextFile.invoke(path)

    override fun writeTextFile(path: String, content: String) {
        writeTextFile.invoke(path, content)
    }

    override fun createDirectory(path: String): Boolean =
        createDirectory.invoke(path)

    override fun deleteFile(path: String): Boolean =
        deleteFile.invoke(path)

    override fun deleteDirectory(path: String): Boolean =
        deleteDirectory.invoke(path)

    override fun renameDirectory(oldPath: String, newPath: String): Boolean =
        renameDirectoryFn(oldPath, newPath)

    override fun exists(path: String): Boolean =
        existsPath(path)

    override fun listDirectory(path: String): List<ChronicleFileEntry> =
        listDirectory.invoke(path)
}
