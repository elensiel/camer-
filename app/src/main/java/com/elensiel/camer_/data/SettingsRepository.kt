package com.elensiel.camer_.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class Settings(
    val aspectRatio: AppAspectRatio = AppAspectRatio.RATIO_4_3,
    val flashEnabled: Boolean = false,
)

class SettingsRepository(private val context: Context) {
    private val aspectKey = stringPreferencesKey("aspect_ratio")
    private val flashKey = booleanPreferencesKey("flash_enabled")

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            aspectRatio = p[aspectKey]
                ?.let { runCatching { AppAspectRatio.valueOf(it) }.getOrNull() }
                ?: AppAspectRatio.RATIO_4_3,
            flashEnabled = p[flashKey] ?: false,
        )
    }

    suspend fun setAspectRatio(ratio: AppAspectRatio) {
        context.dataStore.edit { it[aspectKey] = ratio.name }
    }

    suspend fun setFlashEnabled(enabled: Boolean) {
        context.dataStore.edit { it[flashKey] = enabled }
    }
}
