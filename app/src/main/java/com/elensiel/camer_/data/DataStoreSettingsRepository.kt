package com.elensiel.camer_.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.elensiel.camer_.domain.model.CaptureAspectRatio
import com.elensiel.camer_.domain.model.Settings
import com.elensiel.camer_.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")


class DataStoreSettingsRepository(private val context: Context) : SettingsRepository {
    private val aspectKey = stringPreferencesKey("aspect_ratio")
    private val flashKey = booleanPreferencesKey("flash_enabled")

    override val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            aspectRatio = p[aspectKey]
                ?.let { runCatching { CaptureAspectRatio.valueOf(it) }.getOrNull() }
                ?: CaptureAspectRatio.RATIO_4_3,
            flashEnabled = p[flashKey] ?: false,
        )
    }

    override suspend fun setAspectRatio(ratio: CaptureAspectRatio) {
        context.dataStore.edit { it[aspectKey] = ratio.name }
    }

    override suspend fun setFlashEnabled(enabled: Boolean) {
        context.dataStore.edit { it[flashKey] = enabled }
    }
}
