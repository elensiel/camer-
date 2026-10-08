package com.elensiel.camer_.presentation.camera

import com.elensiel.camer_.domain.model.CaptureAspectRatio
import com.elensiel.camer_.domain.model.ImageFormat
import com.elensiel.camer_.domain.model.LensFacing
import java.io.File

data class CameraUiState(
    val aspectRatio: CaptureAspectRatio = CaptureAspectRatio.RATIO_4_3,
    val flashEnabled: Boolean = false,
    val torchEnabled: Boolean = false,
    val hasFlashUnit: Boolean = true,
    val zoomRatio: Float = 1f,
    val minZoomRatio: Float = 1f,
    val maxZoomRatio: Float = 1f,
    val capturedFile: File? = null,
)
