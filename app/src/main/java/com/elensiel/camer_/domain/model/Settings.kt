package com.elensiel.camer_.domain.model

data class Settings(
    val aspectRatio: CaptureAspectRatio = CaptureAspectRatio.RATIO_4_3,
    val flashEnabled: Boolean = false,
)
