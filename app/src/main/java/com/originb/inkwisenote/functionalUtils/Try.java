package com.originb.inkwisenote.functionalUtils;

import android.util.Log;
import com.originb.inkwisenote.DebugContext;

import java.util.Optional;
import java.util.concurrent.Callable;

public class Try<T> {

    private DebugContext debugContext;
    private Callable<T> callable;
    private Exception exception;

    private String exceptionMessage = "Failed to execute callable";

    private Try(Callable<T> callable, DebugContext debugContext) {
        this.callable = callable;
        this.debugContext = debugContext;
    }


    public static <T> Try<T> to(Callable<T> callable, DebugContext debugContext) {
        return new Try<T>(callable, debugContext);
    }

    public static <T> Try<T> to(Runnable runnable, DebugContext debugContext) {
        Callable<T> callable = () -> {
            runnable.run();
            return null;
        };
        return new Try<T>(callable, debugContext);
    }

    public Try<T> logIfError(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        return this;
    }

    public Optional<T> get() {
        try {
            return Optional.ofNullable(callable.call());
        } catch (Exception e) {
            exception = e;
            Log.e(debugContext.getDebugInfo(), exceptionMessage, exception);
        }

        return Optional.empty();
    }
}

