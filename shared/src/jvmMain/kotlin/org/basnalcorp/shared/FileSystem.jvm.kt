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

actual fun readTextFile(filePath: String): String {
    return File(filePath).readText()
}

actual fun deleteFile(filePath: String): Boolean {
    return try {
        File(filePath).delete()
    } catch (_: Exception) {
        false
    }
}

actual fun deleteDirectory(path: String): Boolean {
    return try {
        File(path).deleteRecursively()
    } catch (_: Exception) {
        false
    }
}

actual fun renameDirectory(oldPath: String, newPath: String): Boolean {
    return try {
        File(oldPath).renameTo(File(newPath))
    } catch (_: Exception) {
        false
    }
}
