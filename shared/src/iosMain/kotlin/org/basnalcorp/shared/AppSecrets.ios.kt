package org.basnalcorp.shared

private object IosAppSecretsHolder {
    var key: String = ""
    var endpoint: String = ""
}

fun setAppSecrets(key: String, endpoint: String) {
    IosAppSecretsHolder.key = key
    IosAppSecretsHolder.endpoint = endpoint
}

actual class AppSecrets actual constructor() {
    actual val visionApiKey: String get() = IosAppSecretsHolder.key
    actual val visionApiEndpoint: String get() = IosAppSecretsHolder.endpoint
}
