package org.basnalcorp.shared.ui

/**
 * Platform hosting the shared Compose UI.
 * Used in [LayoutContext] so screens can adapt (e.g. back affordance, file picker).
 */
enum class Platform {
    Android,
    Desktop
}
