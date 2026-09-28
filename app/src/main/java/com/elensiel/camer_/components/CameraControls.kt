package com.elensiel.camer_.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.elensiel.camer_.CameraHandler
import com.elensiel.camer_.R
import com.elensiel.camer_.data.AppAspectRatio

private val EdgePadding = 24.dp

// ---------------------------------------------------------------
// Screen entry point
// ---------------------------------------------------------------

@Composable
fun CameraControls(
    modifier: Modifier = Modifier,
    cameraHandler: CameraHandler,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Box(modifier = modifier) {

        // -- Top-left: flash & torch (only if the device has a flash unit) --

        if (cameraHandler.hasFlashUnit) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = EdgePadding, top = EdgePadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionButton(
                    onClick = { cameraHandler.toggleFlashMode() },
                    icon = painterResource(
                        if (cameraHandler.flashEnabled) R.drawable.camera_flash_on
                        else R.drawable.camera_flash_off
                    ),
                    contentDescription = "Flash",
                )

                ActionButton(
                    onClick = { cameraHandler.toggleTorch() },
                    icon = painterResource(
                        if (cameraHandler.torchEnabled) R.drawable.torch_on
                        else R.drawable.torch_off
                    ),
                    contentDescription = "Torch",
                )
            }
        }

        // -- Top-right: settings --

        ActionButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = EdgePadding, top = EdgePadding),
            onClick = onSettingsClick,
            icon = painterResource(R.drawable.settings),
            contentDescription = "Settings",
        )

        // -- Bottom: zoom presets, capture row, aspect ratio selector --

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = EdgePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ZoomButtons(cameraHandler = cameraHandler)

            CaptureRow(
                cameraHandler = cameraHandler,
                onCaptureClick = onCaptureClick,
                onGalleryClick = onGalleryClick,
            )

            AspectRatioSelector(cameraHandler = cameraHandler)
        }
    }
}


// ---------------------------------------------------------------
// Sections
// ---------------------------------------------------------------

@Composable
private fun CaptureRow(
    modifier: Modifier = Modifier,
    cameraHandler: CameraHandler,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .glassPill()
            .padding(horizontal = 32.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ActionButton(
            onClick = onGalleryClick,
            icon = painterResource(R.drawable.gallery),
            contentDescription = "Gallery",
        )

        ActionButton(
            onClick = onCaptureClick,
            icon = painterResource(R.drawable.camera_capture),
            contentDescription = "Take photo",
        )

        ActionButton(
            onClick = { cameraHandler.flipCamera() },
            icon = painterResource(R.drawable.camera_flip),
            contentDescription = "Flip camera",
        )
    }
}

@Composable
private fun AspectRatioSelector(
    modifier: Modifier = Modifier,
    cameraHandler: CameraHandler,
) {
    Row(modifier = modifier.glassPill()) {
        AppAspectRatio.entries.forEach { ratio ->
            PillOption(
                text = ratio.label,
                isActive = cameraHandler.aspectRatio == ratio,
                onClick = { cameraHandler.applyAspectRatio(ratio) },
            )
        }
    }
}
