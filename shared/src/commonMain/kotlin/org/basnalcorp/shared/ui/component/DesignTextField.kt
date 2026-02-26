package org.basnalcorp.shared.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import org.basnalcorp.shared.ui.theme.DesignColors
import org.basnalcorp.shared.ui.theme.DesignComponents
import org.basnalcorp.shared.ui.theme.DesignRadius
import org.basnalcorp.shared.ui.theme.DesignSpacing

/**
 * Text input per design.json: height 52.dp, radius 16.dp, background #F7F7F7, border 1px border.light, focus border primary.base.
 */
@Composable
fun DesignTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    focused: Boolean = false
) {
    val shape = RoundedCornerShape(DesignRadius.input)
    val borderColor = if (focused) DesignColors.primaryBase else DesignColors.borderLight

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(DesignComponents.inputHeight)
            .fillMaxWidth()
            .clip(shape)
            .background(DesignColors.inputBackground)
            .border(1.dp, borderColor, shape),
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { inner ->
            Box(modifier = Modifier.padding(horizontal = DesignSpacing.cardPadding, vertical = DesignSpacing.scale12)) {
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                inner()
            }
        }
    )
}
