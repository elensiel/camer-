package com.elensiel.camer_.ui.camera

import androidx.camera.core.CameraSelector
import com.elensiel.camer_.data.AppAspectRatio
import java.io.File

data class CameraUiState(
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val aspectRatio: AppAspectRatio = AppAspectRatio.RATIO_4_3,
    val flashEnabled: Boolean = false,
    val torchEnabled: Boolean = false,
    val hasFlashUnit: Boolean = true,
    val zoomRatio: Float = 1f,
    val minZoomRatio: Float = 1f,
    val maxZoomRatio: Float = 1f,
    val capturedFile: File? = null,
)
