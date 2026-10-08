package com.elensiel.camer_.presentation.settings

import com.elensiel.camer_.domain.model.ImageFormat

data class SettingsUiState(
    val imageFormat: ImageFormat = ImageFormat.JPEG,
    val availableImageFormats: List<ImageFormat> = listOf(ImageFormat.JPEG),
)
