package com.elensiel.camer_.presentation.camera

import android.content.Intent
import android.provider.MediaStore
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun CameraScreen(
    innerPadding: PaddingValues,
    state: CameraUiState,
    surfaceRequest: SurfaceRequest,
    viewModel: CameraViewModel,
    onOpenAdvancedSettings: () -> Unit,
) {
    val context = LocalContext.current

    CameraPreview(
        modifier = Modifier.fillMaxSize(),
        surfaceRequest = surfaceRequest,
        aspectRatio = state.aspectRatio,
        onTapFocus = viewModel::onFocus,
        onZoom = viewModel::onZoomBy,
    )

    CameraControls(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        state = state,
        onCaptureClick = viewModel::onCapture,
        onFlipClick = viewModel::onFlip,
        onFlashClick = viewModel::onToggleFlash,
        onTorchClick = viewModel::onToggleTorch,
        onAspectRatioClick = viewModel::onAspectRatio,
        onZoomPreset = viewModel::onZoomTo,
        onGalleryClick = {
            val intent = Intent(
                Intent.ACTION_VIEW,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            context.startActivity(intent)
        },
        onSettingsClick = {},
        onAdvancedSettingsClick = onOpenAdvancedSettings,
    )
}
