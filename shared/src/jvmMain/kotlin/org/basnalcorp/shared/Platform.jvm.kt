package org.basnalcorp.shared

actual fun platform(): String {
    val osName = System.getProperty("os.name").lowercase()
    return when {
        osName.contains("win") -> "Windows"
        osName.contains("nux") || osName.contains("nix") -> "Linux"
        osName.contains("mac") -> "macOS"
        else -> "Desktop"
    }
}
