package org.basnalcorp.shared

/**
 * Platform-specific app secrets (e.g. API keys). No Android/JVM types in commonMain.
 * - Android: BuildConfig
 * - JVM: env vars or config file
 */
expect class AppSecrets() {
    val visionApiKey: String
    val visionApiEndpoint: String
}
