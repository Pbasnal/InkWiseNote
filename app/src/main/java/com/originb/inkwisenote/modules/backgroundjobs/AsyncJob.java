package com.originb.inkwisenote.modules.backgroundjobs;

import android.os.AsyncTask;
import lombok.Setter;

public abstract class AsyncJob extends AsyncTask<Void, Void, Void> {
    @Setter
    protected boolean continueJob = true;
}
