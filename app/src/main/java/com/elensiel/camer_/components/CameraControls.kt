package com.elensiel.camer_.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elensiel.camer_.CameraHandler
import com.elensiel.camer_.R
import com.elensiel.camer_.data.AppAspectRatio

// ---------------------------------------------------------------
// Shared styling
// ---------------------------------------------------------------

private val GlassColor = Color.White.copy(alpha = 0.20f)
private val PillShape = RoundedCornerShape(50)

private fun Modifier.glassPill(): Modifier = background(GlassColor, PillShape)


// ---------------------------------------------------------------
// Screen entry point
// ---------------------------------------------------------------

@Composable
fun CameraControls(
    modifier: Modifier = Modifier,
    cameraHandler: CameraHandler,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
//    onSettingsClick: () -> Unit,
) {
    Box(modifier = modifier) {

        // -- Top-left: flash & torch (only if the device has a flash unit) --

        if (cameraHandler.hasFlashUnit) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // flash button
                ActionButton(
                    onClick = { cameraHandler.toggleFlashMode() },
                    icon = painterResource(
                        if (cameraHandler.flashEnabled) R.drawable.camera_flash_on
                        else R.drawable.camera_flash_off
                    ),
                    contentDescription = "Flash",
                )

                // torch button
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
                .padding(end = 24.dp, top = 24.dp),
            onClick = {},
            icon = painterResource(R.drawable.settings),
            contentDescription = "Settings",
        )

        // -- Bottom: zoom slider, capture row, aspect ratio selector --

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
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
            val isSelected = cameraHandler.aspectRatio == ratio

            TextButton(
                onClick = { cameraHandler.applyAspectRatio(ratio) }
            ) {
                Text(
                    text = ratio.label,
                    color =
                        if (isSelected) Color.White
                        else Color.White.copy(alpha = 0.5f),
                )
            }
        }
    }
}


// ---------------------------------------------------------------
// Building blocks
// ---------------------------------------------------------------

@Composable
private fun ActionButton(
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
