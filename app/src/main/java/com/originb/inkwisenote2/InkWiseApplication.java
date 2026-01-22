package com.originb.inkwisenote2;

import android.app.Application;
import androidx.work.Configuration;
import androidx.work.WorkManager;

public class InkWiseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize WorkManager with Koin factory before any other initialization
        // This prevents the "WorkManager is already initialized" error
        Configuration workManagerConfiguration = new Configuration.Builder()
                .setWorkerFactory(KoinWorkManagerHelperKt.getKoinWorkManagerFactory())
                .build();
        WorkManager.initialize(this, workManagerConfiguration);
    }
}
