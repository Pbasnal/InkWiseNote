package com.originb.inkwisenote2.functionalUtils;

import com.originb.inkwisenote2.common.Logger;

import java.util.Optional;
import java.util.concurrent.Callable;

public class Try<T> {

    private Logger logger;
    private Callable<T> callable;
    private Exception exception;

    private String exceptionMessage = "Failed to execute callable";

    private Try(Callable<T> callable, Logger logger) {
        this.callable = callable;
        this.logger = logger;
    }


    public static <T> Try<T> to(Callable<T> callable, Logger logger) {
        return new Try<T>(callable, logger);
    }

    public static <T> Try<T> to(Runnable runnable, Logger logger) {
        Callable<T> callable = () -> {
            runnable.run();
            return null;
        };
        return new Try<T>(callable, logger);
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
            logger.exception( exceptionMessage, exception);
        }

        return Optional.empty();
    }
}

