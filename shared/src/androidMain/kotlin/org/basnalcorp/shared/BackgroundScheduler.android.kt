package org.basnalcorp.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Phase 9 will replace with WorkManager; for now use a CoroutineScope.
private val schedulerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

actual class BackgroundScheduler actual constructor() {
    actual fun schedule(block: suspend () -> Unit) {
        schedulerScope.launch { block() }
    }
}
