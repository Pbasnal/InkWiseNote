package org.basnalcorp.shared

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.basnalcorp.shared.db.NotesDatabase
import java.io.File

actual fun createDriver(): SqlDriver {
    val path = File(appStorageRoot(), "notes.db").absolutePath
    val driver = JdbcSqliteDriver("jdbc:sqlite:$path")
    NotesDatabase.Schema.create(driver)
    return driver
}
