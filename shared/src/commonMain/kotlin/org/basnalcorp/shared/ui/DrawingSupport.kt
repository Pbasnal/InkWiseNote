package org.basnalcorp.shared.ui

/**
 * Phase 7.3: Drawing (handwriting) support.
 * Android actual returns true (ML Kit / DrawingView); desktop actual returns false.
 * Full DrawingView in Compose or expect/actual view is Phase 9.
 */
expect fun isDrawingSupported(): Boolean
