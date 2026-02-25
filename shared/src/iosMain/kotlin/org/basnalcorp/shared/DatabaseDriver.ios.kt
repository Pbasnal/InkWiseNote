package org.basnalcorp.shared

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.basnalcorp.shared.db.NotesDatabase

actual fun createDriver(): SqlDriver =
    NativeSqliteDriver(NotesDatabase.Schema, "notes.db")
