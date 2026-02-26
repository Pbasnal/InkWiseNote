package org.basnalcorp.shared

/**
 * Schedules work to run in the background (Phase 9.4).
 * - Android: in-process via CoroutineScope; persistent/deferred work uses app's WorkManager (not in commonMain).
 * - JVM/iOS: CoroutineScope / executor
 */
expect class BackgroundScheduler() {
    fun schedule(block: suspend () -> Unit)
}
