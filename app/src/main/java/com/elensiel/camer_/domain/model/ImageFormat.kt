package com.elensiel.camer_.domain.model

enum class ImageFormat(
    val label: String,
    val fileExtension: String,
    val mimeType: String,
    val needsTranscoding: Boolean = false,
) {
    JPEG("JPEG", "jpg", "image/jpeg"),
    PNG("PNG", "png", "image/png", true),
    RAW("RAW", "dng", "image/x-adobe-dng"),
    ULTRA_HDR("Ultra HDR", "jpg", "image/jpeg"),
    WEBP("WEBP", "webp", "image/webp", true),
}
