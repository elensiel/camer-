package com.elensiel.camer_.data.camera

import android.util.Rational
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import com.elensiel.camer_.domain.model.CaptureAspectRatio
import com.elensiel.camer_.domain.model.ImageFormat
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

internal fun ImageFormat.toCameraXFormat() = when (this) {
    ImageFormat.JPEG -> ImageCapture.OUTPUT_FORMAT_JPEG
    ImageFormat.RAW -> ImageCapture.OUTPUT_FORMAT_RAW
    ImageFormat.ULTRA_HDR -> ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR

    // every format that needs transcoding
    else -> ImageCapture.OUTPUT_FORMAT_JPEG
}

internal fun Int.toImageFormat() = when (this) {
    ImageCapture.OUTPUT_FORMAT_JPEG -> ImageFormat.JPEG
    ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR -> ImageFormat.ULTRA_HDR
    ImageCapture.OUTPUT_FORMAT_RAW -> ImageFormat.RAW
    else -> null
}
