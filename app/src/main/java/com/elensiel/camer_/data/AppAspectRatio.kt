package com.elensiel.camer_.data

import androidx.camera.core.AspectRatio

enum class AppAspectRatio(
    val ratioInt: Int,
    val floatValue: Float,
    val label: String,
) {
    RATIO_4_3(AspectRatio.RATIO_4_3, 3f / 4f, "4:3"),
    RATIO_16_9(AspectRatio.RATIO_16_9, 16f / 9f, "16:9"),

    // CameraX has no native 1:1 constant; falls back to 4:3 internally
    // and relies on cropping elsewhere to achieve a square preview.
    RATIO_1_1(AspectRatio.RATIO_4_3, 1f, "1:1"),
}
