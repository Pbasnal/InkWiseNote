package com.originb.inkwisenote2.functionalUtils

import com.originb.inkwisenote2.common.Logger
import java.util.*
import java.util.concurrent.Callable

class Try<T> private constructor(private val callable: Callable<T?>, private val logger: Logger) {
    private var exception: Exception? = null

    private var exceptionMessage: String? = "Failed to execute callable"


    fun logIfError(exceptionMessage: String?): Try<T> {
        this.exceptionMessage = exceptionMessage
        return this
    }

    fun get(): T? {
        try {
            return callable.call()
        } catch (e: Exception) {
            exception = e
            logger.exception(exceptionMessage, exception)
        }

        return null
    }

    companion object {
        fun <T> to(callable: Callable<T?>, logger: Logger): Try<T> {
            return Try<T>(callable, logger)
        }

        @JvmStatic
        fun <T> to(runnable: Runnable, logger: Logger): Try<T?> {
            val callable: Callable<T?> = Callable {
                runnable.run()
                null
            }
            return Try<T?>(callable, logger)
        }
    }
}

