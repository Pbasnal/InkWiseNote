package org.basnalcorp.shared

/**
 * Call from the iOS app (e.g. in AppDelegate or main) so that [appStorageRoot] returns
 * the app's documents/support directory. Required before using shared DB/storage.
 */
fun setAppStorageRoot(path: String) {
    IosStorageRootHolder.path = path
}

private object IosStorageRootHolder {
    var path: String? = null
}

actual fun appStorageRoot(): String =
    IosStorageRootHolder.path ?: error(
        "App storage root not set. Call org.basnalcorp.shared.setAppStorageRoot(path) from the iOS app."
    )
