package com.originb.inkwisenote2.modules.fileexplorer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import java.io.File
import java.util.*
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
        _toolbarTitle.setValue(if (currentDirectory!!.getName().isEmpty()) "Files" else currentDirectory!!.getName())

        execute(Runnable {
            val fileItems: MutableList<FileItem> = ArrayList<FileItem>()
            val files = currentDirectory!!.listFiles()

            if (files != null && files.size > 0) {
                for (file in files) {
                    fileItems.add(FileItem(file))
                }
                return@execute groupFilesByTimestamp(fileItems)
            }
            ArrayList<FileGroup?>()
        }, Runnable { result ->
            _fileGroups.setValue(result)
            _isRefreshing.setValue(false)
        })
    }

    /**
     * Grouping logic moved from Activity to ViewModel for testability.
     */
    private fun groupFilesByTimestamp(fileItems: MutableList<FileItem>): MutableList<FileGroup?> {
        val groupMap: MutableMap<String?, MutableList<FileItem?>?> = HashMap<String?, MutableList<FileItem?>?>()
        val ungroupedFiles: MutableList<FileItem?> = ArrayList<FileItem?>()

        for (item in fileItems) {
            if (item.isDirectory()) {
                ungroupedFiles.add(item)
                continue
            }

            val timestamp = extractTimestamp(item.getName())
            if (timestamp.isEmpty()) {
                ungroupedFiles.add(item)
            } else {
                if (!groupMap.containsKey(timestamp)) {
                    groupMap.put(timestamp, ArrayList<FileItem?>())
                }
                groupMap.get(timestamp)!!.add(item)
            }
        }

        val result: MutableList<FileGroup?> = ArrayList<FileGroup?>()
        for (entry in groupMap.entries) {
            result.add(FileGroup(entry.key, entry.value))
        }
        for (item in ungroupedFiles) {
            result.add(FileGroup(item))
        }

        Collections.sort<FileGroup?>(result, Comparator { a: FileGroup?, b: FileGroup? ->
            if (a!!.isDirectory() && !b!!.isDirectory()) return@sort -1
            if (!a.isDirectory() && b!!.isDirectory()) return@sort 1
            if (a.isGroup() && !b!!.isGroup()) return@sort -1
            if (!a.isGroup() && b!!.isGroup()) return@sort 1
            if (a.isGroup() && b!!.isGroup()) return@sort b.getTimestamp().compareTo(a.getTimestamp())
            a.getGroupName().compareTo(b!!.getGroupName(), ignoreCase = true)
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
        execute(Runnable {
            var success = true
            if (fileGroup.isGroup()) {
                for (file in fileGroup.getAllRawFiles()) {
                    if (!file.delete()) success = false
                }
            } else {
                val singleFile = fileGroup.getPrimaryFile()
                if (singleFile != null) {
                    if (singleFile.isDirectory()) {
                        success = deleteRecursive(singleFile.getFile())
                    } else {
                        success = singleFile.getFile().delete()
                    }
                }
            }
            success
        }, Runnable { success ->
            if (success) {
                _toastMessage.setValue("Deleted successfully")
                loadCurrentDirectory()
            } else {
                _toastMessage.setValue("Failed to delete some items")
            }
        })
    }

    private fun deleteRecursive(fileOrDirectory: File): Boolean {
        if (fileOrDirectory.isDirectory()) {
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