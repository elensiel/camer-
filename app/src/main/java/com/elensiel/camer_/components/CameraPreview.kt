package com.elensiel.camer_.components

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.elensiel.camer_.CameraHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraHandler: CameraHandler,
) {

    // ---------------------------------------------------------------
    // State
    // ---------------------------------------------------------------

    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    var previewSize by remember { mutableStateOf(IntSize.Zero) }

    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    val focusAlpha by animateFloatAsState(
        targetValue = if (focusPoint == null) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "focusAlpha",
    )

    // ---------------------------------------------------------------
    // Lifecycle / effects
    // ---------------------------------------------------------------

    LaunchedEffect(Unit) {
        cameraHandler.startCamera()
    }

    // Reattaches the surface provider whenever CameraHandler builds a
    // new Preview instance (camera flip, aspect ratio change, etc.)
    LaunchedEffect(cameraHandler.preview) {
        cameraHandler.setSurfaceProvider { request ->
            surfaceRequest = request
        }
    }

    // Clears the focus indicator dot after a short delay
    LaunchedEffect(focusPoint) {
        if (focusPoint != null) {
            delay(800.milliseconds)
            focusPoint = null
        }
    }

    // ---------------------------------------------------------------
    // Gesture handling
    // ---------------------------------------------------------------

    fun Modifier.cameraGestures(): Modifier = pointerInput(cameraHandler, previewSize) {
        coroutineScope {
            launch {
                detectTapGestures { offset ->
                    if (previewSize.width == 0 || previewSize.height == 0) return@detectTapGestures

                    val factory = SurfaceOrientedMeteringPointFactory(
                        previewSize.width.toFloat(),
                        previewSize.height.toFloat(),
                    )
                    val point = factory.createPoint(offset.x, offset.y)

                    cameraHandler.focusOnPoint(point)
                    focusPoint = offset
                }
            }

            launch {
                detectTransformGestures { _, _, zoom, _ ->
                    cameraHandler.zoomBy(zoom)
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------

    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier
                .aspectRatio(cameraHandler.aspectRatio.floatValue)
                .onSizeChanged { previewSize = it }
                .cameraGestures()
                .drawWithContent {
                    drawContent()

                    focusPoint?.let { point ->
                        drawCircle(
                            color = Color.Yellow.copy(alpha = focusAlpha.coerceAtLeast(0.5f)),
                            radius = 45f,
                            center = point,
                            style = Stroke(width = 3f),
                        )
                    }
                }
        )
    }
}
