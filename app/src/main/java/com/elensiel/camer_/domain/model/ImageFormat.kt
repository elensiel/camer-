package com.elensiel.camer_.domain.model

enum class ImageFormat(
    val label: String,
    val fileExtension: String,
    val mimeType: String,
) {
    JPEG("JPEG", "jpg", "image/jpeg"),
    ULTRA_HDR("Ultra HDR", "jpg", "image/jpeg"),
//    RAW("RAW", "dng", "image/x-adobe-dng"),
}
