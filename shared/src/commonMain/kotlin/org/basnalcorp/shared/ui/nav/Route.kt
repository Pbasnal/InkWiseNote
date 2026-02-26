package org.basnalcorp.shared.ui.nav

/**
 * Root navigation routes.
 * Host manages back stack; shared UI only receives current route and onNavigate.
 */
sealed class Route {
    /** Placeholder home / notebook list entry. */
    data object Home : Route()
}
