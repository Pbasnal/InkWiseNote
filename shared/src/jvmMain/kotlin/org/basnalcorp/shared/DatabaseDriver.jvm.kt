package org.basnalcorp.shared

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.basnalcorp.shared.db.NotesDatabase
import java.io.File

actual fun createDriver(): SqlDriver {
    val root = File(appStorageRoot())
    root.mkdirs()
    val dbFile = File(root, "notes.db")
    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
    if (!dbFile.exists() || dbFile.length() == 0L) {
        NotesDatabase.Schema.create(driver)
    }
    return driver
}
