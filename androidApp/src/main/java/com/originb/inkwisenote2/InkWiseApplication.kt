package com.originb.inkwisenote2

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import org.basnalcorp.shared.setAppSecrets
import org.basnalcorp.shared.setAppStorageRoot
import org.basnalcorp.shared.setDriverContext

class InkWiseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Phase 2: provide platform actuals to shared module
        setAppStorageRoot(this)
        setDriverContext(this)
        setAppSecrets(BuildConfig.VISION_API_KEY, BuildConfig.VISION_API_ENDPOINT)

        // Initialize WorkManager with Koin factory before any other initialization
        // This prevents the "WorkManager is already initialized" error
        val workManagerConfiguration = Configuration.Builder()
            .setWorkerFactory(getKoinWorkManagerFactory())
            .build()
        WorkManager.initialize(this, workManagerConfiguration)
    }
}
