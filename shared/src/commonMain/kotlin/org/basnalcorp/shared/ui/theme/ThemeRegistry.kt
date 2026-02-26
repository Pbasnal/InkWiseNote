package org.basnalcorp.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/** Theme identifier. Defaults: [Light] and [Dark]. Extensible for theme packs later. */
enum class ThemeId {
    Light,
    Dark
}

/**
 * Theme data: ColorScheme + Typography from design.json (Inkwise Design System).
 */
data class Theme(
    val colorScheme: ColorScheme,
    val typography: Typography
)

/**
 * Registry of theme id → Theme (ColorScheme + Typography from design.json).
 * Light: background #F5EFE6, primary #2E7D4F, surface card_light/card_tinted.
 * Dark: background dark_mode_base #0F2B2C, surface card_dark #1E3A3B, text inverse.
 */
object ThemeRegistry {
    private val typography = inkwiseTypography()

    private val lightScheme: ColorScheme = lightColorScheme(
        primary = DesignColors.primaryBase,
        onPrimary = DesignColors.primaryContrastText,
        primaryContainer = DesignColors.primaryLight,
        onPrimaryContainer = DesignColors.textPrimary,
        secondary = DesignColors.surfaceCardTinted,
        onSecondary = DesignColors.primaryBase,
        secondaryContainer = DesignColors.surfaceCardTinted,
        onSecondaryContainer = DesignColors.primaryBase,
        tertiary = DesignColors.accentSoftOrange,
        onTertiary = DesignColors.textPrimary,
        background = DesignColors.backgroundPrimary,
        onBackground = DesignColors.textPrimary,
        surface = DesignColors.surfaceCardLight,
        onSurface = DesignColors.textPrimary,
        surfaceVariant = DesignColors.surfaceCardTinted,
        onSurfaceVariant = DesignColors.textSecondary,
        outline = DesignColors.borderLight,
        outlineVariant = DesignColors.borderSubtle,
        error = DesignColors.feedbackError,
        onError = DesignColors.textInverse,
        errorContainer = DesignColors.feedbackError.copy(alpha = 0.2f),
        onErrorContainer = DesignColors.feedbackError
    )

    private val darkScheme: ColorScheme = darkColorScheme(
        primary = DesignColors.primaryLight,
        onPrimary = DesignColors.darkModeBase,
        primaryContainer = DesignColors.primaryBase,
        onPrimaryContainer = DesignColors.primaryContrastText,
        secondary = DesignColors.surfaceCardDark,
        onSecondary = DesignColors.textInverse,
        secondaryContainer = DesignColors.primaryBase.copy(alpha = 0.3f),
        onSecondaryContainer = DesignColors.primaryContrastText,
        tertiary = DesignColors.accentWarm,
        onTertiary = DesignColors.textPrimary,
        background = DesignColors.darkModeBase,
        onBackground = DesignColors.textInverse,
        surface = DesignColors.surfaceCardDark,
        onSurface = DesignColors.textInverse,
        surfaceVariant = DesignColors.surfaceCardDark.copy(alpha = 0.8f),
        onSurfaceVariant = DesignColors.textMuted,
        outline = DesignColors.borderSubtle,
        outlineVariant = DesignColors.borderLight.copy(alpha = 0.5f),
        error = DesignColors.feedbackError,
        onError = DesignColors.textInverse,
        errorContainer = DesignColors.feedbackError.copy(alpha = 0.3f),
        onErrorContainer = DesignColors.feedbackWarning
    )

    private val themes = mapOf(
        ThemeId.Light to Theme(lightScheme, typography),
        ThemeId.Dark to Theme(darkScheme, typography)
    )

    fun get(id: ThemeId): Theme = themes[id] ?: Theme(lightScheme, typography)
}
