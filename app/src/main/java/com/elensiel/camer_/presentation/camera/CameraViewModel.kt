package com.elensiel.camer_.presentation.camera

import android.util.Log
import androidx.camera.core.SurfaceRequest
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elensiel.camer_.CamApp
import com.elensiel.camer_.domain.model.BindConfig
import com.elensiel.camer_.domain.model.CaptureAspectRatio
import com.elensiel.camer_.domain.model.CapturedImage
import com.elensiel.camer_.domain.model.ImageFormat
import com.elensiel.camer_.domain.model.LensFacing
import com.elensiel.camer_.domain.repository.CameraRepository
import com.elensiel.camer_.domain.repository.MediaRepository
import com.elensiel.camer_.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraViewModel(
    private val cameraRepo: CameraRepository,
    private val settingsRepo: SettingsRepository,
    private val mediaRepo: MediaRepository,
) : ViewModel() {
    private val lensFacing = MutableStateFlow(LensFacing.BACK)
    private val capturedPhoto = MutableStateFlow<CapturedImage?>(null)

    val bindConfig: StateFlow<BindConfig?> = combine(
        settingsRepo.settings,
        lensFacing,
    ) { settings, lens ->
        BindConfig(
            lensFacing = lens,
            aspectRatio = settings.aspectRatio,
            imageFormat = settings.imageFormat,
        )
    }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val surfaceRequest: StateFlow<SurfaceRequest?> = cameraRepo.surfaceRequest

    val uiState: StateFlow<CameraUiState> = combine(
        settingsRepo.settings,
        cameraRepo.state,
        capturedPhoto,
    ) { settings, cam, photo ->
        CameraUiState(
            aspectRatio = settings.aspectRatio,
            flashEnabled = settings.flashEnabled,
            torchEnabled = cam.torchEnabled,
            hasFlashUnit = cam.hasFlashUnit,
            zoomRatio = cam.zoomRatio,
            minZoomRatio = cam.minZoomRatio,
            maxZoomRatio = cam.maxZoomRatio,
            capturedFile = photo?.file,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CameraUiState()
    )

    init {
        viewModelScope.launch {
            settingsRepo.settings.map { it.flashEnabled }.distinctUntilChanged()
                .collect(cameraRepo::setFlashEnabled)
        }
    }

    suspend fun bindCamera(
        owner: LifecycleOwner,
        config: BindConfig,
    ) = cameraRepo.bind(
        owner, config.lensFacing, config.aspectRatio, config.imageFormat
    )

    fun onCapture() {
        if (capturedPhoto.value != null) return
        viewModelScope.launch {
            runCatching { cameraRepo.takePhoto() }
                .onSuccess { if (capturedPhoto.value == null) capturedPhoto.value = it }
                .onFailure { Log.e("Camera", "Capture failed.", it) }
        }
    }

    fun onSave() {
        val photo = capturedPhoto.value ?: return
        viewModelScope.launch {
            runCatching { bindConfig.value?.let { mediaRepo.save(photo.file, it.imageFormat) } }
                .onSuccess { capturedPhoto.value = null }
                .onFailure { Log.e("Camera", "Save failed.", it) }
        }
    }

    fun onDiscard() {
        val photo = capturedPhoto.value ?: return
        capturedPhoto.value = null
        viewModelScope.launch { mediaRepo.discard(photo.file) }
    }

    fun onFlip() {
        cameraRepo.setTorch(false)
        lensFacing.update { it.flip() }
    }

    fun onAspectRatio(ratio: CaptureAspectRatio) {
        viewModelScope.launch { settingsRepo.setAspectRatio(ratio) }
    }

    fun onImageFormat(format: ImageFormat) {
        viewModelScope.launch { settingsRepo.setImageFormat(format) }
    }

    fun onToggleFlash() {
        viewModelScope.launch { settingsRepo.setFlashEnabled(!uiState.value.flashEnabled) }
    }

    fun onToggleTorch() = cameraRepo.setTorch(!uiState.value.torchEnabled)
    fun onFocus(point: Offset) = cameraRepo.focusOn(point.x, point.y)
    fun onZoomBy(factor: Float) = cameraRepo.zoomBy(factor)
    fun onZoomTo(ratio: Float) = cameraRepo.setZoom(ratio)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as CamApp).container
                CameraViewModel(
                    c.cameraRepository,
                    c.settingsRepository,
                    c.mediaRepository,
                )
            }
        }
    }
}
