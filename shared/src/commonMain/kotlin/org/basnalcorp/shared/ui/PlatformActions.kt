package org.basnalcorp.shared.ui

/**
 * Platform UI actions (Phase 7.3): back, file picker, toast.
 * Host (Android/Desktop) provides implementation; shared screens use for navigation and feedback.
 */
interface PlatformActions {
    /** Navigate back (e.g. pop back stack or system back). */
    fun back()

    /** Show a file picker; callback receives selected path or null if cancelled. */
    fun showFilePicker(callback: (String?) -> Unit)

    /** Show a short message (toast/snackbar). */
    fun showToast(message: String)
}
