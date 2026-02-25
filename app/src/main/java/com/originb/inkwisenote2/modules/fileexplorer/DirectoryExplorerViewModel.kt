package com.originb.inkwisenote2.modules.fileexplorer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import java.io.File
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer
import java.util.regex.Matcher
import java.util.regex.Pattern

class DirectoryExplorerViewModel : ViewModel() {
    private val _fileGroups = MutableLiveData<MutableList<FileGroup?>?>()
    val fileGroups: LiveData<MutableList<FileGroup?>?> = _fileGroups

    private val _isRefreshing = MutableLiveData<Boolean?>()
    val isRefreshing: LiveData<Boolean?> = _isRefreshing

    private val _toolbarTitle = MutableLiveData<String?>()
    val toolbarTitle: LiveData<String?> = _toolbarTitle

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    var currentDirectory: File? = null
        private set
    private val navigationHistory = Stack<File?>()

    /**
     * Initialize the explorer with a starting directory.
     */
    fun init(initialDir: File?) {
        if (currentDirectory == null) {
            currentDirectory = initialDir
            loadCurrentDirectory()
        }
    }

    fun loadCurrentDirectory() {
        if (currentDirectory == null) return

        _isRefreshing.setValue(true)
        _toolbarTitle.setValue(if (currentDirectory!!.name.isEmpty()) "Files" else currentDirectory!!.name)

        BackgroundOps.execute(Callable {
            val fileItems = ArrayList<FileItem>()
            val files = currentDirectory!!.listFiles()
            if (files != null && files.isNotEmpty()) {
                for (file in files) {
                    fileItems.add(FileItem(file))
                }
                groupFilesByTimestamp(fileItems)
            } else {
                ArrayList<FileGroup?>()
            }
        }, Consumer { result: MutableList<FileGroup?>? ->
            _fileGroups.setValue(result)
            _isRefreshing.setValue(false)
        })
    }

    /**
     * Grouping logic moved from Activity to ViewModel for testability.
     */
    private fun groupFilesByTimestamp(fileItems: MutableList<FileItem>): MutableList<FileGroup?> {
        val groupMap = HashMap<String?, MutableList<FileItem>>()
        val ungroupedFiles = ArrayList<FileItem>()

        for (item in fileItems) {
            if (item.isDirectory) {
                ungroupedFiles.add(item)
                continue
            }
            val timestamp = extractTimestamp(item.name)
            if (timestamp.isEmpty()) {
                ungroupedFiles.add(item)
            } else {
                groupMap.getOrPut(timestamp) { ArrayList() }.add(item)
            }
        }

        val result = ArrayList<FileGroup?>()
        for (entry in groupMap.entries) {
            result.add(FileGroup(entry.key, entry.value))
        }
        for (item in ungroupedFiles) {
            result.add(FileGroup(item))
        }

        Collections.sort(result, Comparator { a: FileGroup?, b: FileGroup? ->
            when {
                a!!.isDirectory && !b!!.isDirectory -> -1
                !a.isDirectory && b!!.isDirectory -> 1
                a.isGroup && !b!!.isGroup -> -1
                !a.isGroup && b!!.isGroup -> 1
                a.isGroup && b!!.isGroup -> (b.timestamp ?: "").compareTo(a.timestamp ?: "")
                else -> (a.groupName ?: "").compareTo(b!!.groupName ?: "", ignoreCase = true)
            }
        })
        return result
    }

    private fun extractTimestamp(filename: String?): String {
        if (filename == null) return ""
        val matcher: Matcher = TIMESTAMP_PATTERN.matcher(filename)
        return if (matcher.find()) matcher.group(1) else ""
    }

    /**
     * Navigation Logic
     */
    fun navigateInto(directory: File?) {
        navigationHistory.push(currentDirectory)
        currentDirectory = directory
        loadCurrentDirectory()
    }

    fun navigateBack(): Boolean {
        if (!navigationHistory.isEmpty()) {
            currentDirectory = navigationHistory.pop()
            loadCurrentDirectory()
            return true
        }
        return false
    }

    /**
     * Delete Logic
     */
    fun deleteFileGroup(fileGroup: FileGroup) {
        BackgroundOps.execute(Callable {
            var success = true
            if (fileGroup.isGroup) {
                for (file in fileGroup.allRawFiles) {
                    if (file != null && !file.delete()) success = false
                }
            } else {
                fileGroup.primaryFile?.let { singleFile ->
                    success = if (singleFile.isDirectory) {
                        deleteRecursive(singleFile.file)
                    } else {
                        singleFile.file.delete()
                    }
                }
            }
            success
        }, Consumer { success: Boolean? ->
            if (success == true) {
                _toastMessage.setValue("Deleted successfully")
                loadCurrentDirectory()
            } else {
                _toastMessage.setValue("Failed to delete some items")
            }
        })
    }

    private fun deleteRecursive(fileOrDirectory: File): Boolean {
        if (fileOrDirectory.isDirectory) {
            val children = fileOrDirectory.listFiles()
            if (children != null) {
                for (child in children) {
                    if (!deleteRecursive(child)) return false
                }
            }
        }
        return fileOrDirectory.delete()
    }

    companion object {
        // Timestamp pattern regex
        private val TIMESTAMP_PATTERN: Pattern = Pattern.compile("^(\\d{8,14})(?:[\\.-]|$)")
    }
}