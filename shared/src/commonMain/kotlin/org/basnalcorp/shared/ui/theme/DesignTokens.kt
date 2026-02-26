package org.basnalcorp.shared.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design tokens from design.json (Inkwise Design System).
 * Soft Editorial Productivity; earthy-neutral with green primary.
 */

// ---------- Colors (light theme as default refs; dark uses same names, different values in ThemeRegistry) ----------

object DesignColors {
    // Background
    val backgroundPrimary = Color(0xFFF5EFE6)
    val backgroundSecondary = Color(0xFFFFFFFF)
    val darkModeBase = Color(0xFF0F2B2C)

    // Primary
    val primaryBase = Color(0xFF2E7D4F)
    val primaryHover = Color(0xFF276B43)
    val primaryLight = Color(0xFF4FA36E)
    val primaryContrastText = Color(0xFFFFFFFF)

    // Accent
    val accentWarm = Color(0xFFE6A85C)
    val accentSoftOrange = Color(0xFFE9B46A)

    // Surface
    val surfaceCardLight = Color(0xFFFFFFFF)
    val surfaceCardTinted = Color(0xFFEDF3EE)
    val surfaceCardDark = Color(0xFF1E3A3B)

    // Text
    val textPrimary = Color(0xFF1E1E1E)
    val textSecondary = Color(0xFF5F6368)
    val textInverse = Color(0xFFFFFFFF)
    val textMuted = Color(0xFF9AA0A6)

    // Border
    val borderLight = Color(0xFFE6E6E6)
    val borderSubtle = Color(0xFFF0F0F0)

    // Feedback
    val feedbackSuccess = Color(0xFF4CAF50)
    val feedbackWarning = Color(0xFFF4A261)
    val feedbackError = Color(0xFFE76F51)

    // Input background (design.json inputs.background)
    val inputBackground = Color(0xFFF7F7F7)
}

// ---------- Spacing (design.json spacing_system) ----------

object DesignSpacing {
    val baseUnit: Dp = 4.dp
    val scale4: Dp = 4.dp
    val scale8: Dp = 8.dp
    val scale12: Dp = 12.dp
    val scale16: Dp = 16.dp
    val scale20: Dp = 20.dp
    val scale24: Dp = 24.dp
    val scale32: Dp = 32.dp
    val scale40: Dp = 40.dp
    val scale48: Dp = 48.dp
    val scale64: Dp = 64.dp

    val layoutPaddingMobile: Dp = 20.dp
    val cardPadding: Dp = 16.dp
    val sectionSpacing: Dp = 24.dp
}

// ---------- Radius (design.json radius_system) ----------

object DesignRadius {
    val button: Dp = 24.dp
    val card: Dp = 20.dp
    val input: Dp = 16.dp
    val pill: Dp = 999.dp
    val modal: Dp = 28.dp
}

// ---------- Shadows (design.json shadow_system); use with Modifier.shadow(elevation, shape) ----------
// Soft: offset_y 4, blur 16, rgba 0.05 → elevation ~2.dp; medium: 8, 24, 0.08 → ~4.dp. none_on_dark: true.

object DesignShadow {
    val softElevation: Dp = 2.dp
    val mediumElevation: Dp = 4.dp
}

// ---------- Component dimensions (design.json component_system) ----------

object DesignComponents {
    val primaryButtonHeight: Dp = 52.dp
    val secondaryButtonHeight: Dp = 48.dp
    val ghostButtonHeight: Dp = 44.dp
    val touchTargetMin: Dp = 44.dp
    val chipHeight: Dp = 36.dp
    val inputHeight: Dp = 52.dp
    val topBarHeight: Dp = 56.dp
    val topBarIconSize: Dp = 24.dp
    val listItemHeight: Dp = 56.dp
    val chipPaddingHorizontal: Dp = 16.dp
}
