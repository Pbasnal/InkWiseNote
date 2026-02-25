package org.basnalcorp.shared

/**
 * Returns the root directory path for app storage (DB, files, etc.).
 * - Android: call [org.basnalcorp.shared.setAppStorageRoot] with Context from Application/Activity.
 * - JVM (Desktop): user.home/.inkwisenote (no setup required).
 * - iOS: call [org.basnalcorp.shared.setAppStorageRoot] with the app support path from the iOS app.
 */
expect fun appStorageRoot(): String
