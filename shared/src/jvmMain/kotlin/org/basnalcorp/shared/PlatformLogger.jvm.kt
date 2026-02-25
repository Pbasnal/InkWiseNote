package org.basnalcorp.shared

actual class PlatformLogger actual constructor() {
    actual fun log(level: LogLevel, tag: String, message: String) {
        val line = "[$level] $tag: $message"
        when (level) {
            LogLevel.ERROR -> System.err.println(line)
            else -> println(line)
        }
    }
}
