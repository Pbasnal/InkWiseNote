package org.basnalcorp.shared.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.basnalcorp.shared.ui.theme.DesignColors
import org.basnalcorp.shared.ui.theme.DesignComponents
import org.basnalcorp.shared.ui.theme.DesignRadius
import org.basnalcorp.shared.ui.theme.DesignSpacing

/**
 * Chip per design.json: height 36.dp, pill radius, padding horizontal 16.dp;
 * selected: primary.base bg, text.inverse; unselected: card_tinted bg, text.primary.
 */
@Composable
fun DesignChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) DesignColors.primaryBase else DesignColors.surfaceCardTinted
    val textColor = if (selected) DesignColors.textInverse else DesignColors.textPrimary
    val shape = RoundedCornerShape(DesignRadius.pill)

    Box(
        modifier = modifier
            .height(DesignComponents.chipHeight)
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = DesignComponents.chipPaddingHorizontal),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}
