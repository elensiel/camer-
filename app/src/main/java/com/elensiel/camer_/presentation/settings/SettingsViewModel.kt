package com.elensiel.camer_.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elensiel.camer_.CamApp
import com.elensiel.camer_.domain.model.ImageFormat
import com.elensiel.camer_.domain.repository.CameraRepository
import com.elensiel.camer_.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    cameraRepo: CameraRepository,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepo.settings,
        cameraRepo.state,
    ) { settings, cam ->
        val available = ImageFormat.entries.filter { it in cam.supportedFormats }

        SettingsUiState(
            imageFormat =
                if (settings.imageFormat in available) settings.imageFormat
                else ImageFormat.JPEG,
            availableImageFormats = available,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState()
    )

    fun onImageFormat(format: ImageFormat) {
        viewModelScope.launch { settingsRepo.setImageFormat(format) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as CamApp).container
                SettingsViewModel(c.cameraRepository, c.settingsRepository)
            }
        }
    }
}
