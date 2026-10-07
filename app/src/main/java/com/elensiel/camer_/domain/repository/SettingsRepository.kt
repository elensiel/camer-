package com.elensiel.camer_.domain.repository

import com.elensiel.camer_.domain.model.CaptureAspectRatio
import com.elensiel.camer_.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<Settings>

    suspend fun setAspectRatio(ratio: CaptureAspectRatio)
    suspend fun setFlashEnabled(enabled: Boolean)
}
