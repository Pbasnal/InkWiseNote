package com.originb.inkwisenote.modules.backgroundjobs;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import androidx.annotation.NonNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class BackgroundJobScheduler {

    private Map<Type, JobInfo> jobInfoMap = new HashMap<>();
    private static AtomicInteger atomicInteger = new AtomicInteger(0);
    private JobScheduler jobScheduler;

    public void scheduleJob(Context context, @NonNull Class<?> jobServiceType) {
        if (jobInfoMap.containsKey(jobServiceType)) {
            return;
        }
        jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        ComponentName componentName = new ComponentName(context, AsyncJobController.class);
        JobInfo jobInfo = new JobInfo.Builder(atomicInteger.getAndIncrement(), componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(false) // Persist across reboots
                .setBackoffCriteria(0, 10 * 1000)
                .build();

        jobScheduler.schedule(jobInfo);
        jobInfoMap.put(jobServiceType, jobInfo);
    }

    public void terminateJob(Type jobType) {
        JobInfo jobInfo = jobInfoMap.getOrDefault(jobType, null);
        if (Objects.isNull(jobInfo)) {
            return;
        }

        jobScheduler.cancel(jobInfo.getId());
    }
}
