package com.originb.inkwisenote.modules.messaging;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class BackgroundOps {

    private static BackgroundOps instance;
    private final Handler mainThreadHandler;

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    public static BackgroundOps getInstance() {
        if (instance == null) {
            instance = new BackgroundOps();
        }
        return instance;
    }

    private BackgroundOps() {
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public static void execute(Runnable runnable) {
        getInstance().executor.execute(runnable);

    }

    public static <T> void execute(Callable<T> callable, Consumer<T> resultOnMainThread) {
        try {
            Future<T> callFuture = getInstance().executor.submit(callable);
            T result = callFuture.get();

            getInstance().mainThreadHandler.post(() -> resultOnMainThread.accept(result));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
