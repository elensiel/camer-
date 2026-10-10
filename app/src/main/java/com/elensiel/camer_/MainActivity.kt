package com.elensiel.camer_

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elensiel.camer_.domain.model.CaptureAspectRatio
import com.elensiel.camer_.presentation.camera.CameraControls
import com.elensiel.camer_.presentation.camera.CameraScreen
import com.elensiel.camer_.presentation.camera.CameraUiState
import com.elensiel.camer_.presentation.camera.CameraViewModel
import com.elensiel.camer_.presentation.review.PhotoReviewScreen
import com.elensiel.camer_.presentation.settings.SettingsScreen
import com.elensiel.camer_.presentation.theme.CamerTheme
import com.elensiel.permission.PermissionData
import com.elensiel.permission.PermissionGate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CamerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionGate(permissions = permissions) {
                        App(innerPadding)
                    }
                }
            }
        }
    }

    companion object {
        private val permissions = listOf(
            PermissionData(
                Manifest.permission.CAMERA,
                "Camera",
                "Needed to take photos with the app.",
            )
        )
    }
}

@Composable
fun App(
    innerPadding: PaddingValues,
    viewModel: CameraViewModel = viewModel(factory = CameraViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val config by viewModel.bindConfig.collectAsStateWithLifecycle()
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var showSettings by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(config, lifecycleOwner) {
        val c = config ?: return@LaunchedEffect
        viewModel.bindCamera(lifecycleOwner, c)
    }

    val file = state.capturedPhotoFile

    when {
        file != null -> {
            PhotoReviewScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                photoFile = file,
                onSave = viewModel::onSave,
                onDiscard = viewModel::onDiscard,
            )
        }

        showSettings -> SettingsScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            onBack = { showSettings = false },
        )

        else -> {
            surfaceRequest?.let {
                CameraScreen(
                    innerPadding = innerPadding,
                    state = state,
                    surfaceRequest = it,
                    viewModel = viewModel,
                    onOpenSettings = { showSettings = true },
                )
            }
        }
    }
}


// ---------------------------------------------------------------
// Debug previews (viewfinder replaced by a black box)
// ---------------------------------------------------------------

@Composable
private fun CameraUiDebug(initial: CameraUiState) {
    var state by remember { mutableStateOf(initial) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        CameraControls(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 24.dp), // approximate system bar insets
            state = state,
            onCaptureClick = {},
            onFlipClick = {},
            onFlashClick = { state = state.copy(flashEnabled = !state.flashEnabled) },
            onTorchClick = { state = state.copy(torchEnabled = !state.torchEnabled) },
            onAspectRatioClick = { state = state.copy(aspectRatio = it) },
            onZoomPreset = { state = state.copy(zoomRatio = it) },
            onGalleryClick = {},
            onSettingsClick = {},
        )
    }
}

@Preview(name = "Camera UI - Back Cam", showBackground = true)
@Composable
private fun CameraUiFullPreview() {
    CamerTheme {
        CameraUiDebug(
            CameraUiState(
                aspectRatio = CaptureAspectRatio.RATIO_4_3,
                hasFlashUnit = true,
                zoomRatio = 1f,
                minZoomRatio = 0.5f,
                maxZoomRatio = 40f,
            )
        )
    }
}

@Preview(name = "Camera UI - Front Cam", showBackground = true)
@Composable
private fun CameraUiFrontPreview() {
    CamerTheme {
        CameraUiDebug(
            CameraUiState(
                aspectRatio = CaptureAspectRatio.RATIO_16_9,
                hasFlashUnit = false,
            )
        )
    }
}
