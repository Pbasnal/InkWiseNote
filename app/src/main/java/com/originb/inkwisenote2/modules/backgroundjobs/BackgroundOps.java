package com.originb.inkwisenote2.modules.backgroundjobs;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.Optional;
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

    public static void execute(Runnable runnable, Runnable continueOnMainThread) {
        try {
            getInstance().executor.execute(runnable);
            getInstance().mainThreadHandler.post(continueOnMainThread::run);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void execute(Callable<T> callable, Consumer<T> resultOnMainThread) {
        // 1. Capture the stack trace of the CALLER (the UI thread) right now
        Exception clientStack = new Exception("Task submitted from here");

        getInstance().executor.execute(() -> {
            try {
                T result = callable.call();
                getInstance().mainThreadHandler.post(() -> resultOnMainThread.accept(result));
            } catch (Exception e) {
                // 2. Stitch the current background exception with the caller's stack trace
                e.initCause(clientStack);

                // 3. Now when you log this, you see exactly where it was enqueued
                handleError(e);
            }
        });
    }

    public static <T> void executeOpt(Callable<Optional<T>> callable, Consumer<T> resultOnMainThread) {
        getInstance().executor.execute(() -> {
            try {
                Optional<T> result = callable.call();
                if (result.isPresent()) {
                    getInstance().mainThreadHandler.post(() -> resultOnMainThread.accept(result.get()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void handleError(Exception e) {
        // Using a logger helps preserve the full cause chain
        e.printStackTrace();
        Log.e("BackgroundOps", "Background task failed", e);

        // Optional: Post back to UI to show an error dialog/toast
        getInstance().mainThreadHandler.post(() -> {
            // You could add an Error Consumer to your execute method if needed
        });
    }
}
