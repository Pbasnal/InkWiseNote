namespace Systems.BackgroundJob;

public class JobDetails
{
    public Action Job { get; set; }
    public int WaitTimeBetweenRunsInMs { get; set; }
    public int WaitTimeBeforeJobStarts { get; set; }
}

public class RunningJob
{
    public CancellationTokenSource CancellationTokenSource { get; set; }
    public Task JobTask { get; set; }
}

public class JobSystem
{
    private IDictionary<string, JobDetails> jobDirectory;
    private IDictionary<string, RunningJob> runningJobs;


    public JobSystem()
    {
        jobDirectory = new Dictionary<string, JobDetails>();
        runningJobs = new Dictionary<string, RunningJob>();
    }

    public void RegisterJob(string jobName, Action job, int waitTimeBeforeJobStarts, int waitTimeBetweenRunsInMs)
    {
        if (jobDirectory.ContainsKey(jobName))
        {
            jobDirectory.Remove(jobName);
        }

        jobDirectory.Add(jobName, new JobDetails
        {
            Job = job,
            WaitTimeBetweenRunsInMs = waitTimeBetweenRunsInMs,
            WaitTimeBeforeJobStarts = waitTimeBeforeJobStarts,
        });
    }

    public void StartJob(string jobName)
    {
        if (runningJobs.ContainsKey(jobName))
        {
            return;
        }

        CancellationTokenSource tknSrc = new CancellationTokenSource();
        var jobDetails = jobDirectory[jobName];
        var jobTask = JobWrapper(tknSrc.Token, jobDetails);

        runningJobs.Add(jobName, new RunningJob
        {
            JobTask = jobTask,
            CancellationTokenSource = tknSrc
        });
    }

    public void StopJob(string jobName)
    {
        if (!runningJobs.ContainsKey(jobName)) return;

        runningJobs[jobName].CancellationTokenSource.Cancel();
        runningJobs.Remove(jobName);    
    }

    public async Task JobWrapper(CancellationToken cancellationToken, JobDetails jobDetails)
    {
        await Task.Delay(jobDetails.WaitTimeBeforeJobStarts);

        while (!cancellationToken.IsCancellationRequested)
        {
            jobDetails.Job.Invoke();

            await Task.Delay(jobDetails.WaitTimeBetweenRunsInMs);
        }
    }
}
