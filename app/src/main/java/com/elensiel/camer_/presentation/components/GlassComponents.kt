package com.elensiel.camer_.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val GlassColor = Color.White.copy(alpha = 0.20f)
internal val InactiveContentColor = Color.White.copy(alpha = 0.5f)
internal val PillShape = RoundedCornerShape(50)

internal fun Modifier.glassPill(): Modifier = background(GlassColor, PillShape)

@Composable
internal fun ActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String,
    size: Dp = 48.dp,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .background(
                color = GlassColor,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Color.White,
                shape = CircleShape
            ),
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = Color.White,
        )
    }
}

/** A text option inside a glass pill; the active one is bright and bold. */
@Composable
internal fun PillOption(
    modifier: Modifier = Modifier,
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(
            text = text,
            color = if (isActive) Color.White else InactiveContentColor,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
