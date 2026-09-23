package com.elensiel.camer_.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String,
    size: Dp = 48.dp,
) {
    IconButton(
        onClick = onClick, modifier = modifier
            .size(size)
            .background(
                color = Color.White.copy(alpha = 0.20f),
                shape = RoundedCornerShape(360.dp),
            )
            .border(
                width = 1.dp,
                color = Color.White,
                shape = CircleShape,
            )
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = Color.White,
        )
    }
}
