package org.basnalcorp.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Phase 9.4: In-process background only. App uses WorkManager (TextParsingWorker etc.) separately for persistent work.
private val schedulerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

actual class BackgroundScheduler actual constructor() {
    actual fun schedule(block: suspend () -> Unit) {
        schedulerScope.launch { block() }
    }
}
