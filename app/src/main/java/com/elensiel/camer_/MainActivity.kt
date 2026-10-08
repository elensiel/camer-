package com.elensiel.camer_

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elensiel.camer_.presentation.camera.CameraScreen
import com.elensiel.camer_.presentation.camera.CameraViewModel
import com.elensiel.camer_.presentation.review.ImageReviewScreen
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

    LaunchedEffect(config, lifecycleOwner) {
        val c = config ?: return@LaunchedEffect
        viewModel.bindCamera(lifecycleOwner, c)
    }

    val file = state.capturedFile

    when {
        file == null -> {
            surfaceRequest?.let {
                CameraScreen(
                    innerPadding = innerPadding,
                    state = state,
                    surfaceRequest = it,
                    viewModel = viewModel,
                )
            }
        }

        else -> {
            ImageReviewScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                photoFile = file,
                onSave = viewModel::onSave,
                onDiscard = viewModel::onDiscard,
            )
        }
    }
}
