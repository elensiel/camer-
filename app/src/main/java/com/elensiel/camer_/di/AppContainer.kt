package com.elensiel.camer_.di

import android.content.Context
import com.elensiel.camer_.data.CameraXRepository
import com.elensiel.camer_.data.MediaStoreRepository
import com.elensiel.camer_.data.DataStoreSettingsRepository
import com.elensiel.camer_.domain.repository.SettingsRepository

class AppContainer(context: Context) {
    val settingsRepository: SettingsRepository = DataStoreSettingsRepository(context)
    val cameraRepository = CameraXRepository(context)
    val mediaRepository = MediaStoreRepository(context, mimeType = "jpeg", saveDirectory = "DCIM/camer-")
}
