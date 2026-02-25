package org.basnalcorp.shared

import app.cash.sqldelight.db.SqlDriver

/**
 * Creates a SQLDelight driver for the current platform.
 * - Android: AndroidSqliteDriver (Context must be set via setAppStorageRoot/setDriverContext).
 * - JVM: JvmSqliteDriver (file under appStorageRoot()).
 */
expect fun createDriver(): SqlDriver
