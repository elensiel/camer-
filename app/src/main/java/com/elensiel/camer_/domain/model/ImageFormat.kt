package com.elensiel.camer_.domain.model

enum class ImageFormat(
    val label: String,
    val fileExtension: String,
    val mimeType: String,
) {
    JPEG("JPEG", "jpeg", "image/jpeg"),
    ULTRA_HDR("Ultra HDR", "jpeg_r", "image/jpeg_r"),
    RAW("Raw", "raw", "image/raw"),
}
