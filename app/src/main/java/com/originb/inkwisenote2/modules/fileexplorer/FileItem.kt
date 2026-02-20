package com.originb.inkwisenote2.modules.fileexplorer

import java.io.File

/**
 * Model class representing a file or directory in the file explorer
 */
class FileItem(val file: File) {
    val name: String
    val isDirectory: Boolean
    val lastModified: Long
    val size: Long

    init {
        this.name = file.getName()
        this.isDirectory = file.isDirectory()
        this.lastModified = file.lastModified()
        this.size = file.length()
    }

    val path: String
        get() = file.getAbsolutePath()
}