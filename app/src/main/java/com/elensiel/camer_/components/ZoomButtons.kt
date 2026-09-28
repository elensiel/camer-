package com.elensiel.camer_.components

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.elensiel.camer_.CameraHandler
import kotlin.math.roundToInt

private val ZoomPresets = listOf(0.5f, 1f, 2f, 5f, 10f, 40f)

// tolerance when comparing zoom ratios (floats never match exactly)
private const val ZOOM_EPSILON = 0.05f

@Composable
internal fun ZoomButtons(
    modifier: Modifier = Modifier,
    cameraHandler: CameraHandler,
) {
    val zoom = cameraHandler.zoomRatio
    val minZoom = cameraHandler.minZoomRatio
    val maxZoom = cameraHandler.maxZoomRatio

    // only offer presets the current camera can actually reach
    val presets = remember(minZoom, maxZoom) {
        ZoomPresets.filter {
            it >= minZoom - ZOOM_EPSILON && it <= maxZoom + ZOOM_EPSILON
        }
    }

    // not enough range to be worth showing
    if (presets.size < 2) return

    val activeIndex = presets
        .indexOfLast { zoom >= it - ZOOM_EPSILON }
        .coerceAtLeast(0)

    Row(modifier = modifier.glassPill()) {
        presets.forEachIndexed { index, preset ->
            val isActive = index == activeIndex

            PillOption(
                // the active button shows the live zoom instead of its preset
                text = formatZoom(if (isActive) zoom else preset),
                isActive = isActive,
                onClick = { cameraHandler.applyZoomRatio(preset) },
            )
        }
    }
}

// 1.0 -> "1x", 2.34 -> "2.3x"
private fun formatZoom(value: Float): String {
    val rounded = (value * 10f).roundToInt() / 10f
    return if (rounded % 1f == 0f) "${rounded.toInt()}x" else "${rounded}x"
}
