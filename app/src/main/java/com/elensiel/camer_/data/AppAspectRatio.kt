package com.elensiel.camer_.data

import android.util.Rational
import androidx.camera.core.AspectRatio

enum class AppAspectRatio(
    val ratioInt: Int,
    val floatValue: Float,
    val label: String,
    val viewPortRational: Rational,
) {
    RATIO_16_9(
        AspectRatio.RATIO_16_9,
        9f / 16f,
        "16:9",
        Rational(9, 16),
    ),

    RATIO_4_3(
        AspectRatio.RATIO_4_3,
        3f / 4f,
        "4:3",
        Rational(3, 4),
    ),

    // Sensor stream is still 4:3; the ViewPort crops it to a square.
    RATIO_1_1(
        AspectRatio.RATIO_4_3,
        1f,
        "1:1",
        Rational(1, 1),
    ),
}
