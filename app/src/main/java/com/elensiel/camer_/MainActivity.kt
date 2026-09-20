package com.elensiel.camer_

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.elensiel.camer_.components.CameraControls
import com.elensiel.camer_.components.CameraPreview
import com.elensiel.camer_.components.CapturedImagePreview
import com.elensiel.camer_.ui.theme.CamerTheme
import com.tomatorangers.tomaito.permission.PermissionGate
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CamerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionGate {
                        App(innerPadding)
                    }
                }
            }
        }
    }
}

@Composable
fun App(
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraHandler = remember {
        CameraHandler(
            context = context,
            lifecycleOwner = lifecycleOwner,
        )
    }

    val capturedImageHandler = remember {
        CapturedImageHandler(
            context,
            "png",
            "DCIM/camer-",
        )
    }

    var capturedImage by remember { mutableStateOf<File?>(null) }

    when {
        capturedImage == null -> {
            CameraScreen(
                innerPadding = innerPadding,
                context = context,
                cameraHandler = cameraHandler,
                onPhotoCaptured = { capturedImage = it }
            )
        }

        else -> {
            CapturedImagePreview(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                photoFile = capturedImage!!,

                onSave = {
                    capturedImageHandler.saveImage(
                        photoFile = capturedImage!!,
                        onSaved = { capturedImage = null },
                        onError = { exception ->
                            Log.e("Camera", "Save failed", exception)
                        }
                    )
                },

                onDiscard = {
                    capturedImageHandler.discardPhoto(capturedImage!!)
                    capturedImage = null
                }
            )
        }
    }
}

@Composable
fun CameraScreen(
    innerPadding: PaddingValues,
    context: Context,
    cameraHandler: CameraHandler,
    onPhotoCaptured: (File) -> Unit,
) {
    CameraPreview(
        modifier = Modifier.fillMaxSize(),
        cameraHandler,
    )

    CameraControls(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        cameraHandler = cameraHandler,

        onCaptureClick = {
            cameraHandler.takePhoto(
                onPhotoCaptured = onPhotoCaptured,
                onError = { exception ->
                    Log.d(
                        "Camera",
                        "Capture failed",
                        exception
                    )
                }
            )
        },

        onGalleryClick = {
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                setDataAndType(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "image/*"
                )
            }

            context.startActivity(intent)
        },

//        onSettingsClick = {},
    )
}


// UI DEBUGGING
@Preview
@Composable
fun CameraScreenPreview() {
    CameraControls(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        CameraHandler(
            LocalContext.current,
            LocalLifecycleOwner.current,
        ),
        {},
        {},
    )
}
