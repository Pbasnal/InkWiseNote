package com.originb.inkwisenote2.modules.smarthome.utils

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.originb.inkwisenote2.R

/**
 * Espresso helpers for the Smart Home navigation drawer.
 */
object SmartHomeDrawerHelper {

    /**
     * Opens the drawer (start gravity). Toolbar hamburger triggers the same;
     * this uses DrawerLayout directly for reliability.
     */
    fun openDrawer() {
        onView(withId(R.id.drawer_layout))
            .perform(DrawerActions.open())
    }

    /** Closes the drawer. */
    fun closeDrawer() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close())
    }

    /** Opens drawer and taps the "Queries" nav item. */
    fun openDrawerAndTapQueries() {
        openDrawer()
        onView(withId(R.id.nav_view)).perform(
            NavigationViewActions.navigateTo(R.id.nav_queries)
        )
    }

    /** Opens drawer and taps the "File explorer" nav item. */
    fun openDrawerAndTapFileExplorer() {
        openDrawer()
        onView(withId(R.id.nav_view)).perform(
            NavigationViewActions.navigateTo(R.id.nav_file_explorer)
        )
    }

    /** Opens drawer and taps the "Admin" nav item. */
    fun openDrawerAndTapAdmin() {
        openDrawer()
        onView(withId(R.id.nav_view)).perform(
            NavigationViewActions.navigateTo(R.id.admin_button)
        )
    }
}
