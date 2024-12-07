package com.originb.inkwisenote.modules.backgroundjobs;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class AsyncJobController extends JobService {
    private TextProcessingJob job;

    @Override
    public boolean onStartJob(JobParameters params) {
        job = new TextProcessingJob(this, params);
        job.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        job.setContinueJob(false);
        return true;
    }
}


