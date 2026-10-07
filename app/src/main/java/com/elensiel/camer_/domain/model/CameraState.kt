package com.elensiel.camer_.domain.model

data class CameraState(
    val hasFlashUnit: Boolean = true,
    val torchEnabled: Boolean = false,
    val zoomRatio: Float = 1f,
    val minZoomRatio: Float = 1f,
    val maxZoomRatio: Float = 1f,
)
