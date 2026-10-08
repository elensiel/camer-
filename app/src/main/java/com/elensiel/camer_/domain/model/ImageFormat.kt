package com.elensiel.camer_.domain.model

enum class ImageFormat(
    val label: String,
    val fileExtension: String,
    val mimeType: String,
) {
    JPEG("JPEG", "jpeg", "image/jpeg"),
    ULTRA_HDR("Ultra HDR", "jpg", "image/jpeg_r"),
    RAW("RAW", "dng", "image/x-adobe-dng"),
}
