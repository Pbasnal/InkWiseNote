package org.basnalcorp.shared.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.basnalcorp.shared.FileExplorerItem
import org.basnalcorp.shared.appStorageRoot
import org.basnalcorp.shared.createDirectory
import org.basnalcorp.shared.listDirectory

/**
 * State holder for FileExplorerScreen (Phase 3).
 * Loads directory listing via [listDirectory]; when [initialPath] is null, uses
 * app storage + "/notes" (Chronicle notes root) so test notebooks are visible.
 * Call [load] when route appears (e.g. LaunchedEffect(initialPath) { load(initialPath) }).
 */
class FileExplorerStateHolder {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _currentPath = MutableStateFlow<String?>(null)
    val currentPath: StateFlow<String?> = _currentPath.asStateFlow()
    private val _items = MutableStateFlow<List<FileExplorerItem>>(emptyList())
    val items: StateFlow<List<FileExplorerItem>> = _items.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Back stack of paths for "navigate back". */
    private val navigationStack = mutableListOf<String>()

    val canGoBack: Boolean
        get() = navigationStack.isNotEmpty()

    /**
     * Load directory at [initialPath], or Chronicle notes folder (app storage + "/notes") if null,
     * so that Chronicle test notebooks are visible when opening File explorer.
     */
    fun load(initialPath: String?) {
        val path = when {
            initialPath?.isNotBlank() == true -> initialPath
            else -> appStorageRoot() + "/notes"
        }
        if (path == appStorageRoot() + "/notes") {
            createDirectory(path)
        }
        navigationStack.clear()
        _currentPath.value = path
        refresh()
    }

    /**
     * Reload current directory (pull-to-refresh).
     */
    fun refresh() {
        val path = _currentPath.value ?: return
        scope.launch {
            _isLoading.value = true
            _error.value = null
            _items.value = listDirectory(path)
            _isLoading.value = false
        }
    }

    /**
     * Navigate into a directory (push current path and load the child).
     */
    fun navigateInto(path: String) {
        val current = _currentPath.value ?: return
        navigationStack.add(current)
        _currentPath.value = path
        refresh()
    }

    /**
     * Go back to previous directory. Returns true if navigated.
     */
    fun navigateBack(): Boolean {
        if (navigationStack.isEmpty()) return false
        _currentPath.value = navigationStack.removeAt(navigationStack.lastIndex)
        refresh()
        return true
    }
}
