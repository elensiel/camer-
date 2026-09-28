package com.elensiel.camer_.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.elensiel.camer_.CameraHandler

private val ZoomPresets = listOf(0.5f, 1f, 2f, 5f, 10f)
private const val EPSILON = 0.05f

@Composable
fun ZoomButtons(
    modifier: Modifier = Modifier,
    cameraHandler: CameraHandler,
) {
    val zoom = cameraHandler.zoomRatio

    val presets = ZoomPresets.filter {
        it >= cameraHandler.minZoomRatio - EPSILON &&
                it <= cameraHandler.maxZoomRatio + EPSILON
    }
    if (presets.size < 2) return

    val activeIndex = presets
        .indexOfLast { zoom >= it - EPSILON }
        .coerceAtLeast(0)

    Row(
        modifier = modifier.background(
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(50)
        )
    ) {
        presets.forEachIndexed { index, preset ->
            val isActive = index == activeIndex

            TextButton(
                onClick = { cameraHandler.applyZoomRatio(preset) },
            ) {
                Text(
                    text =
                        if (isActive) formatZoom(zoom)
                        else formatZoom(preset),
                    color =
                        if (isActive) Color.White
                        else Color.White.copy(alpha = 0.5f),
                    fontWeight =
                        if (isActive) FontWeight.Bold
                        else FontWeight.Normal,
                )
            }
        }
    }
}

private fun formatZoom(value: Float): String {
    val rounded = Math.round(value * 10f) / 10f
    return if (rounded * 1f == 0f) "${rounded.toInt()}x" else "${rounded}x"
}
