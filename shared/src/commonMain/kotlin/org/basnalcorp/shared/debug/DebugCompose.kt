package org.basnalcorp.shared.debug

/**
 * Debug hook to report which Compose runtime (Composer) is loaded at runtime.
 * Used to diagnose NoSuchMethodError: Composer.shouldExecute(boolean, int).
 * Call once when the note detail screen is composed (before switching to Preview).
 */
expect fun reportComposerDebug()
