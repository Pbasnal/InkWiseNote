package com.originb.inkwisenote2.modules.backgroundjobs;

import android.os.Handler;
import android.os.Looper;

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
        try {
            Future<T> callFuture = getInstance().executor.submit(callable);
            T result = callFuture.get();

            getInstance().mainThreadHandler.post(() -> resultOnMainThread.accept(result));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void executeOpt(Callable<Optional<T>> callable, Consumer<T> resultOnMainThread) {
        try {
            Future<Optional<T>> callFuture = getInstance().executor.submit(callable);
            Optional<T> result = callFuture.get();

            if (result.isPresent()) {
                getInstance().mainThreadHandler.post(() -> resultOnMainThread.accept(result.get()));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
