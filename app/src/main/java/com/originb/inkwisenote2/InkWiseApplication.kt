package com.originb.inkwisenote2

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class InkWiseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize WorkManager with Koin factory before any other initialization
        // This prevents the "WorkManager is already initialized" error
        val workManagerConfiguration = Configuration.Builder()
            .setWorkerFactory(getKoinWorkManagerFactory())
            .build()
        WorkManager.initialize(this, workManagerConfiguration)
    }
}
