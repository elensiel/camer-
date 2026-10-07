package com.elensiel.camer_.data.camera

import android.util.Rational
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import com.elensiel.camer_.domain.model.CaptureAspectRatio
import com.elensiel.camer_.domain.model.LensFacing

internal fun CaptureAspectRatio.toCameraXRatio() = when (this) {
    CaptureAspectRatio.RATIO_16_9 -> AspectRatio.RATIO_16_9
    CaptureAspectRatio.RATIO_4_3,
    CaptureAspectRatio.RATIO_1_1 -> AspectRatio.RATIO_4_3
}

internal fun CaptureAspectRatio.toViewPortRational() = when (this) {
    CaptureAspectRatio.RATIO_16_9 -> Rational(9, 16)
    CaptureAspectRatio.RATIO_4_3 -> Rational(3, 4)
    CaptureAspectRatio.RATIO_1_1 -> Rational(1, 1)
}

internal fun LensFacing.toCameraX() = when (this) {
    LensFacing.BACK -> CameraSelector.LENS_FACING_BACK
    LensFacing.FRONT -> CameraSelector.LENS_FACING_FRONT
}
