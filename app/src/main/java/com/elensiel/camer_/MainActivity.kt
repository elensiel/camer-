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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.elensiel.camer_.components.CameraControls
import com.elensiel.camer_.components.CameraPreview
import com.elensiel.camer_.components.CapturedImagePreview
import com.elensiel.camer_.data.AppAspectRatio
import com.elensiel.camer_.processors.image.SquareCropProcessor
import com.elensiel.camer_.ui.theme.CamerTheme
import com.elensiel.permission.PermissionGate
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CamerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionGate(permissions = CameraPermissions.permissions) {
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
    val coroutineScope = rememberCoroutineScope()
    val sharedPreferences = remember {
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    }

    val cameraHandler = remember {
        CameraHandler(
            context = context,
            lifecycleOwner = lifecycleOwner,
            prefs = sharedPreferences,
        )
    }
    val capturedImageHandler = remember {
        CapturedImageHandler(
            context,
            "jpg",
            "DCIM/camer-",
        )
    }
    val squareCropProcessor = remember { SquareCropProcessor() }

    var capturedImage by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    when {
        capturedImage == null -> {
            CameraScreen(
                innerPadding = innerPadding,
                context = context,
                cameraHandler = cameraHandler,
                onPhotoCaptured = {
                    // guard against duplicate captures
                    if (capturedImage != null) return@CameraScreen



                    if (cameraHandler.aspectRatio == AppAspectRatio.RATIO_1_1) {
                        isProcessing = true
                        coroutineScope.launch {
                            capturedImage = squareCropProcessor.process(it)
                            isProcessing = false
                        }
                    } else {
                        capturedImage = it
                    }
                },
            )

            if (isProcessing) LoadingLayer(innerPadding)
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
private fun CameraScreen(
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
                    Log.e("Camera", "Capture failed", exception)
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

        onSettingsClick = {},
    )
}

@Composable
private fun LoadingLayer(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(
                color = Color.Black.copy(alpha = 0.5f)
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White.copy(alpha = 0.8f))
    }
}


// UI DEBUGGING
@Preview
@Composable
private fun CameraScreenPreview() {
    CameraControls(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        CameraHandler(
            LocalContext.current,
            LocalLifecycleOwner.current,
            prefs = LocalContext.current.getSharedPreferences(
                "user_preferences",
                Context.MODE_PRIVATE
            ),
        ),
        {},
        {},
        {},
    )

//    LoadingLayer()
}
