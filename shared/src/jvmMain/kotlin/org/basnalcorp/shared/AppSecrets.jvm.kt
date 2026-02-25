package org.basnalcorp.shared

actual class AppSecrets actual constructor() {
    actual val visionApiKey: String
        get() = System.getenv("VISION_API_KEY") ?: ""
    actual val visionApiEndpoint: String
        get() = System.getenv("VISION_API_ENDPOINT") ?: ""
}
