package org.basnalcorp.shared.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography from design.json (Inkwise Design System).
 * Scale: display 28/700, heading_l 22/600, heading_m 18/600, body_l 16/400, body_m 14/400, caption 12/400.
 * Letter-spacing heading -0.3px; body 0.
 */
fun inkwiseTypography(): Typography {
    val headingLetterSpacing = (-0.3).sp
    return Typography(
        displayLarge = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = (28 * 1.2).sp,
            letterSpacing = headingLetterSpacing
        ),
        displayMedium = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = (28 * 1.2).sp,
            letterSpacing = headingLetterSpacing
        ),
        displaySmall = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = (28 * 1.2).sp,
            letterSpacing = headingLetterSpacing
        ),
        headlineLarge = TextStyle(
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (22 * 1.3).sp,
            letterSpacing = headingLetterSpacing
        ),
        headlineMedium = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (18 * 1.3).sp,
            letterSpacing = headingLetterSpacing
        ),
        headlineSmall = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (18 * 1.3).sp,
            letterSpacing = headingLetterSpacing
        ),
        titleLarge = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (18 * 1.3).sp,
            letterSpacing = headingLetterSpacing
        ),
        titleMedium = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (16 * 1.5).sp
        ),
        titleSmall = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (14 * 1.5).sp
        ),
        bodyLarge = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (16 * 1.5).sp
        ),
        bodyMedium = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (14 * 1.5).sp
        ),
        bodySmall = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (12 * 1.4).sp
        ),
        labelLarge = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (14 * 1.5).sp
        ),
        labelMedium = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (12 * 1.4).sp
        ),
        labelSmall = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (12 * 1.4).sp
        )
    )
}
