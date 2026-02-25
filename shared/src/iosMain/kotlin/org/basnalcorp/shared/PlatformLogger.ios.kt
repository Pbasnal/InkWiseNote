package org.basnalcorp.shared

actual class PlatformLogger actual constructor() {
    actual fun log(level: LogLevel, tag: String, message: String) {
        println("[$level] $tag: $message")
    }
}
