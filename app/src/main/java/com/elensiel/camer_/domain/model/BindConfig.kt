package com.elensiel.camer_.domain.model

data class BindConfig(
    val lensFacing: LensFacing,
    val aspectRatio: CaptureAspectRatio,
    val imageFormat: ImageFormat,
)
