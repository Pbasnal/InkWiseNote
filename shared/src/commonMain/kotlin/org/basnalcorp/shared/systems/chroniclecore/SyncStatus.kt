package org.basnalcorp.shared.systems.chroniclecore

/**
 * Status events sent on the resync status channel so clients can track sync progress.
 */
sealed class SyncStatus {

    data object Idle : SyncStatus()

    data class SyncingStatus(
        val notebookId: String,
        val syncedNotebookCount: Int,
        val totalNotebookCount: Int
    ) : SyncStatus()

    data object Done : SyncStatus()

    data class Error(val message: String) : SyncStatus()
}
