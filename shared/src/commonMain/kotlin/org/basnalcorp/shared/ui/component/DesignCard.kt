package org.basnalcorp.shared.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.basnalcorp.shared.ui.theme.DesignColors
import org.basnalcorp.shared.ui.theme.DesignRadius
import org.basnalcorp.shared.ui.theme.DesignShadow
import org.basnalcorp.shared.ui.theme.DesignSpacing

/**
 * Card per design.json: radius 20.dp, padding 16.dp, background surface.card_light, soft shadow.
 * Content structure: optional header, title, supporting text, optional media, optional action.
 */
@Composable
fun DesignCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(DesignRadius.card)
    val isDark = MaterialTheme.colorScheme.background == DesignColors.darkModeBase
    val elevation = if (isDark) 0.dp else DesignShadow.softElevation
    val clickModifier = if (onClick != null) Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    ) else Modifier

    Card(
        modifier = modifier.then(clickModifier).clip(shape),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation,
            pressedElevation = elevation,
            hoveredElevation = elevation,
            focusedElevation = elevation,
            draggedElevation = elevation
        )
    ) {
        Box(modifier = Modifier.padding(DesignSpacing.cardPadding)) {
            content()
        }
    }
}
