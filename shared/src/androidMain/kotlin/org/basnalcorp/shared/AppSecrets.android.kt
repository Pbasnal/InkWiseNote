package org.basnalcorp.shared

// BuildConfig is provided by the consuming app (androidApp); shared does not have its own.
// We use a holder set by the app so shared stays free of app-specific BuildConfig.
// Phase 5 will wire: app provides BuildConfig values via Koin or setAppSecrets().

private object AppSecretsHolder {
    var key: String = ""
    var endpoint: String = ""
}

fun setAppSecrets(key: String, endpoint: String) {
    AppSecretsHolder.key = key
    AppSecretsHolder.endpoint = endpoint
}

actual class AppSecrets actual constructor() {
    actual val visionApiKey: String get() = AppSecretsHolder.key
    actual val visionApiEndpoint: String get() = AppSecretsHolder.endpoint
}
