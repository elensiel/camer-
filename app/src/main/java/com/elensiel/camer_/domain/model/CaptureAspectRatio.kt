package com.elensiel.camer_.domain.model

enum class CaptureAspectRatio(
    val floatValue: Float,
    val label: String,
) {
    RATIO_16_9(
        9f / 16f,
        "16:9",
    ),

    RATIO_4_3(
        3f / 4f,
        "4:3",
    ),

    // Sensor stream is still 4:3; the ViewPort crops it to a square.
    RATIO_1_1(
        1f,
        "1:1",
    ),
}
