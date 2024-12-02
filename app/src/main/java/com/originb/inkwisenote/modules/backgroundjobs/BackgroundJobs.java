package com.originb.inkwisenote.modules.backgroundjobs;

import android.content.ContextWrapper;

import java.util.ArrayList;
import java.util.List;

public class BackgroundJobs {

    private List<Class<?>> jobServices = new ArrayList<>();
    private BackgroundJobScheduler backgroundJobScheduler = new BackgroundJobScheduler();


    private static BackgroundJobs instance;

    public static BackgroundJobs getInstance() {
        if (instance == null) {
            instance = new BackgroundJobs();
        }
        return instance;
    }

    public static void registerJobs(ContextWrapper appContext) {
        getInstance().registerJobsInternal(appContext);
    }

    private void registerJobsInternal(ContextWrapper appContext) {
        jobServices.add(AsyncJobController.class);
    }

    public static void scheduleJobs(ContextWrapper appContext) {
        for (Class<?> jobservice : getInstance().jobServices) {
            getInstance().backgroundJobScheduler.scheduleJob(appContext, jobservice);
        }
    }
}
