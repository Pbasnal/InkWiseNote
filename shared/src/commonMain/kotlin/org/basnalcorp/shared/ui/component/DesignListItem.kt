package org.basnalcorp.shared.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.basnalcorp.shared.ui.theme.DesignColors
import org.basnalcorp.shared.ui.theme.DesignComponents
import org.basnalcorp.shared.ui.theme.DesignSpacing

/**
 * List item per design.json: min height 56.dp, optional subtle divider, left text + optional right icon; touch target ≥ 44.dp.
 */
@Composable
fun DesignListItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(DesignComponents.listItemHeight)
            .padding(horizontal = DesignSpacing.layoutPaddingMobile)
            .then(
                if (showDivider) Modifier.padding(bottom = 1.dp)
                else Modifier
            )
            .then(Modifier.clickable(onClick = onClick)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) trailing()
    }
}
