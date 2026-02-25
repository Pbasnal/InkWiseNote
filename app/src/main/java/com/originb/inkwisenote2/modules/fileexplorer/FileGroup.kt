package com.originb.inkwisenote2.modules.fileexplorer

import java.io.File

/**
 * Model class representing a group of files with the same timestamp prefix
 */
class FileGroup {
    val timestamp: String?
    var groupName: String? = null
        private set
    val files: MutableList<FileItem>
    val isGroup: Boolean

    // Constructor for a group of files
    constructor(timestamp: String?, files: MutableList<FileItem>) {
        this.timestamp = timestamp
        this.files = files
        this.isGroup = true


        // Set a meaningful group name
        if (!files.isEmpty()) {
            // Use the first file's name but remove the timestamp to make it cleaner
            val firstFileName = files.get(0).name
            if (firstFileName.contains("-")) {
                this.groupName = firstFileName.substring(firstFileName.indexOf("-") + 1)
            } else {
                this.groupName = firstFileName
            }
        } else {
            this.groupName = "Group " + timestamp
        }
    }

    // Constructor for a single file (not part of a group)
    constructor(singleFile: FileItem) {
        this.files = ArrayList<FileItem>()
        this.files.add(singleFile)
        this.isGroup = false
        this.groupName = singleFile.name
        this.timestamp = ""
    }

    val fileCount: Int
        get() = files.size

    val primaryFile: FileItem?
        get() = if (files.isEmpty()) null else files.get(0)

    val isDirectory: Boolean
        get() {
            val primaryFile = this.primaryFile
            return primaryFile != null && primaryFile.isDirectory
        }

    val allRawFiles: MutableList<File?>
        get() {
            val rawFiles: MutableList<File?> = ArrayList<File?>()
            for (item in files) {
                rawFiles.add(item.file)
            }
            return rawFiles
        }
}