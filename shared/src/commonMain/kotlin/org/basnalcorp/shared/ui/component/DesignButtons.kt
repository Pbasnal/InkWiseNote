package org.basnalcorp.shared.ui.component

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.basnalcorp.shared.ui.theme.DesignColors
import org.basnalcorp.shared.ui.theme.DesignComponents
import org.basnalcorp.shared.ui.theme.DesignRadius

/**
 * Primary button per design.json: height 52.dp, radius 24.dp, fill primary.base, text inverse, font weight 600.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(DesignComponents.primaryButtonHeight),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = DesignColors.primaryBase,
            contentColor = DesignColors.textInverse,
            disabledContainerColor = DesignColors.primaryBase.copy(alpha = 0.5f),
            disabledContentColor = DesignColors.textInverse.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(DesignRadius.button)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Secondary button per design.json: height 48.dp, radius 24.dp, fill surface.card_tinted, text primary.base.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(DesignComponents.secondaryButtonHeight),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = DesignColors.surfaceCardTinted,
            contentColor = DesignColors.primaryBase,
            disabledContainerColor = DesignColors.surfaceCardTinted.copy(alpha = 0.6f),
            disabledContentColor = DesignColors.primaryBase.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(DesignRadius.button)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Ghost button per design.json: height 44.dp, transparent, text primary.base; touch target ≥ 44.dp.
 */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(DesignComponents.ghostButtonHeight),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = DesignColors.primaryBase,
            disabledContentColor = DesignColors.primaryBase.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
