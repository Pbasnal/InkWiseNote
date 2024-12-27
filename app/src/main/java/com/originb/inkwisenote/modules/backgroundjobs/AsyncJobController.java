package com.originb.inkwisenote.modules.backgroundjobs;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import com.originb.inkwisenote.config.ConfigReader;

import java.util.ArrayList;
import java.util.List;

public class AsyncJobController extends JobService {
    private List<AsyncJob> asyncTasks;

    @Override
    public boolean onStartJob(JobParameters params) {
        asyncTasks = new ArrayList<>();

        if (ConfigReader.isAzureOcrEnabled()) {
            asyncTasks.add(new TextParsingJob());
            asyncTasks.add(new TextProcessingJob(this, params));
        }
        asyncTasks.forEach(AsyncJob::execute);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        asyncTasks.forEach(j -> j.setContinueJob(false));
        return true;
    }
}

