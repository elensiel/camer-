package com.elensiel.camer_

import android.app.Application
import android.content.Context
import com.elensiel.camer_.data.CameraRepository
import com.elensiel.camer_.data.MediaRepository
import com.elensiel.camer_.data.SettingsRepository

class App : Application() {
    val container by lazy { AppContainer(this) }
}

class AppContainer(context: Context) {
    val settingsRepository = SettingsRepository(context)
    val cameraRepository = CameraRepository(context)
    val mediaRepository = MediaRepository(context, mimeType = "jpeg", saveDirectory = "DCIM/camer-")
}
