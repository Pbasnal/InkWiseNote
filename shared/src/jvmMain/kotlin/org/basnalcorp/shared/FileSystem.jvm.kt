package org.basnalcorp.shared

import java.io.File

actual fun listDirectory(path: String): List<FileExplorerItem> {
    return try {
        val dir = File(path)
        if (!dir.isDirectory) return emptyList()
        dir.listFiles()?.map { file ->
            FileExplorerItem(
                path = file.absolutePath,
                name = file.name,
                isDirectory = file.isDirectory
            )
        }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}

actual fun createDirectory(path: String): Boolean {
    return try {
        File(path).mkdirs()
    } catch (_: Exception) {
        false
    }
}

actual fun createNotebookDirectory(parentPath: String, title: String): String {
    val dir = File(parentPath, title)
    dir.mkdirs()
    return dir.absolutePath
}

actual fun writeTextFile(filePath: String, content: String) {
    val file = File(filePath)
    file.parentFile?.mkdirs()
    file.writeText(content)
}
