package org.basnalcorp.shared

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.basnalcorp.shared.db.NotesDatabase

private object DriverContextHolder {
    var context: Context? = null
}

/**
 * Call from Application or main Activity so that [createDriver] can create the DB.
 * Typically called together with [setAppStorageRoot].
 */
fun setDriverContext(context: Context) {
    DriverContextHolder.context = context
}

actual fun createDriver(): SqlDriver {
    val ctx = DriverContextHolder.context
        ?: error("Driver context not set. Call org.basnalcorp.shared.setDriverContext(context) from Application.")
    return AndroidSqliteDriver(NotesDatabase.Schema, ctx, "notes.db")
}
