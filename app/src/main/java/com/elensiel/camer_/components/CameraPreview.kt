package com.elensiel.camer_.components

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.elensiel.camer_.CameraHandler

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraHandler: CameraHandler,
) {
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }

    LaunchedEffect(Unit) {
        cameraHandler.setSurfaceProvider { request ->
            surfaceRequest = request
        }

        cameraHandler.startCamera()
    }

    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier
                .pointerInput(cameraHandler) {
                    detectTransformGestures { _, _, zoom, _ ->
                        cameraHandler.zoomBy(zoom)
                    }
                }
        )
    }
}
