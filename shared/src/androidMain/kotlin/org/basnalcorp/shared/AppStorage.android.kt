package org.basnalcorp.shared

import android.content.Context

/**
 * Call from Application or main Activity (e.g. Application.onCreate) so that
 * [appStorageRoot] returns the app's files directory. Required before using shared DB/storage.
 */
fun setAppStorageRoot(context: Context) {
    AppStorageRootHolder.path = context.filesDir?.absolutePath ?: context.cacheDir?.absolutePath
        ?: error("Could not resolve app storage path")
}

private object AppStorageRootHolder {
    var path: String? = null
}

actual fun appStorageRoot(): String =
    AppStorageRootHolder.path ?: error(
        "App storage root not set. Call org.basnalcorp.shared.setAppStorageRoot(context) from Application or main Activity."
    )
