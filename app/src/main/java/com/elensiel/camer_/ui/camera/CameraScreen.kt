package com.elensiel.camer_.ui.camera

import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun CameraScreen(
    innerPadding: PaddingValues,
    state: CameraUiState,
    surfaceRequest: SurfaceRequest,
    viewModel: CameraViewModel,
) {
    val context = LocalContext.current

    CameraPreview(
        modifier = Modifier.fillMaxSize(),
        surfaceRequest = surfaceRequest,
        aspectRatio = state.aspectRatio,
        onTapFocus = viewModel::onFocus,
        onZoom = viewModel::onZoomBy,
    )
}
