package com.elensiel.camer_.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.elensiel.camer_.CameraHandler
import com.elensiel.camer_.R

@Composable
fun CameraControls(
    modifier: Modifier = Modifier,
    cameraHandler: CameraHandler,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
//    onSettingsClick: () -> Unit,
) {
    Box(
        modifier = modifier
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 24.dp, top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // show flash & torch button
            // only if it has flash unit
            if (cameraHandler.hasFlashUnit) {
                // flash button
                ActionButton(
                    onClick = { cameraHandler.toggleFlashMode() },
                    icon =
                        if (cameraHandler.flashEnabled) {
                            painterResource(R.drawable.camera_flash_on)
                        } else {
                            painterResource(R.drawable.camera_flash_off)
                        },
                    contentDescription = "Torch",
                )

                // torch button
                ActionButton(
                    onClick = { cameraHandler.toggleTorch() },
                    icon =
                        if (cameraHandler.torchEnabled) {
                            painterResource(R.drawable.torch_on)
                        } else {
                            painterResource(R.drawable.torch_off)
                        },
                    contentDescription = "Torch",
                )
            }
        }


        // settings button
        ActionButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 24.dp, top = 24.dp),
            onClick = {},
            icon = painterResource(R.drawable.settings),
            contentDescription = "Settings"
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {

//            Row(
//                modifier = Modifier
//                    .background(
//                        color = Color.White.copy(alpha = 0.35f),
//                        shape = RoundedCornerShape(50)
//                    )
//                    .padding(horizontal = 16.dp, vertical = 1.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                Text("LIVE")
//
//                var isLiveMode by remember { mutableStateOf(false) }
//
//                Switch(
//                    checked = isLiveMode,
//                    onCheckedChange = { isLiveMode = it }
//                )
//            }

            Row(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // gallery button
                ActionButton(
                    onClick = onGalleryClick,
                    icon = painterResource(R.drawable.gallery),
                    contentDescription = "Gallery"
                )

                // capture button
                ActionButton(
                    onClick = onCaptureClick,
                    icon = painterResource(R.drawable.camera_capture),
                    contentDescription = "Take photo"
                )

                // flip camera button
                ActionButton(
                    onClick = { cameraHandler.flipCamera() },
                    icon = painterResource(R.drawable.camera_flip),
                    contentDescription = "Flip camera"
                )
            }
        }
    }
}
