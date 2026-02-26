package org.basnalcorp.shared.util

import org.basnalcorp.shared.LogLevel
import org.basnalcorp.shared.PlatformLogger

/**
 * Runs a block and captures exceptions; logs via [PlatformLogger] on failure.
 * Use in commonMain where no platform-specific Callable/Runnable is available.
 */
class Try<T> private constructor(
    private val block: () -> T?,
    private val logger: PlatformLogger,
    private val tag: String = "Try"
) {
    private var exception: Throwable? = null

    fun logIfError(exceptionMessage: String): Try<T> {
        if (exception != null) {
            logger.log(LogLevel.ERROR, tag, "$exceptionMessage: ${exception?.message}")
            exception?.let { logger.log(LogLevel.ERROR, tag, it.stackTraceToString()) }
        }
        return this
    }

    fun get(): T? {
        return try {
            block()
        } catch (e: Throwable) {
            exception = e
            logger.log(LogLevel.ERROR, tag, e.message ?: "Failed to execute")
            logger.log(LogLevel.ERROR, tag, e.stackTraceToString())
            null
        }
    }

    companion object {
        fun <T> to(block: () -> T?, logger: PlatformLogger, tag: String = "Try"): Try<T> =
            Try(block, logger, tag)
    }
}
