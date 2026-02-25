package org.basnalcorp.shared

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

/**
 * Platform-specific logger. Use in commonMain for logging without platform types.
 * - Android: android.util.Log
 * - JVM: println or slf4j
 */
expect class PlatformLogger() {
    fun log(level: LogLevel, tag: String, message: String)
}
