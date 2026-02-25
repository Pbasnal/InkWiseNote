package org.basnalcorp.shared

/**
 * Schedules work to run in the background.
 * - Android: WorkManager (Phase 9) or CoroutineScope for now.
 * - JVM: CoroutineScope / executor
 */
expect class BackgroundScheduler() {
    fun schedule(block: suspend () -> Unit)
}
