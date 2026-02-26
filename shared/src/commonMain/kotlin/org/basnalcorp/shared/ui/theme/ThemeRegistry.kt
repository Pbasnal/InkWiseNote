package org.basnalcorp.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/** Theme identifier. Defaults: [Light] and [Dark]. Extensible for theme packs later. */
enum class ThemeId {
    Light,
    Dark
}

/**
 * Registry of theme id → ColorScheme.
 * Selection can be default (e.g. [ThemeId.Light]) now; user preference wired in Phase 6/7.
 */
object ThemeRegistry {
    private val schemes = mapOf(
        ThemeId.Light to lightColorScheme(),
        ThemeId.Dark to darkColorScheme()
    )

    fun get(id: ThemeId): ColorScheme = schemes[id] ?: lightColorScheme()
}
