package com.elensiel.camer_.ui.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.MeteringPoint
import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elensiel.camer_.App
import com.elensiel.camer_.data.AppAspectRatio
import com.elensiel.camer_.data.CameraRepository
import com.elensiel.camer_.data.MediaRepository
import com.elensiel.camer_.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class CameraViewModel(
    private val cameraRepo: CameraRepository,
    private val settingsRepo: SettingsRepository,
    private val mediaRepo: MediaRepository,
) : ViewModel() {
    private val lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
    private val capturedFile = MutableStateFlow<File?>(null)

    val surfaceRequest: StateFlow<SurfaceRequest?> = cameraRepo.surfaceRequest

    val uiState: StateFlow<CameraUiState> = combine(
        settingsRepo.settings,
        cameraRepo.state,
        lensFacing,
        capturedFile,
    ) { settings, cam, lens, file ->
        CameraUiState(
            lensFacing = lens,
            aspectRatio = settings.aspectRatio,
            flashEnabled = settings.flashEnabled,
            torchEnabled = cam.torchEnabled,
            hasFlashUnit = cam.hasFlashUnit,
            zoomRatio = cam.zoomRatio,
            minZoomRatio = cam.minZoomRatio,
            maxZoomRatio = cam.maxZoomRatio,
            capturedFile = file
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

    suspend fun bindCamera(owner: LifecycleOwner, lens: Int, ratio: AppAspectRatio) =
        cameraRepo.bind(owner, lens, ratio)

    fun onCapture() {
        if (capturedFile.value != null) return
        viewModelScope.launch {
            runCatching { cameraRepo.takePhoto() }
                .onSuccess { if (capturedFile.value == null) capturedFile.value = it }
                .onFailure { Log.e("Camera", "Capture failed.", it) }
        }
    }

    fun onSave() {
        val file = capturedFile.value ?: return
        capturedFile.value = null
        viewModelScope.launch { mediaRepo.discard(file) }
    }

    fun onDiscard() {
        val file = capturedFile.value ?: return
        capturedFile.value = null
        viewModelScope.launch { mediaRepo.discard(file) }
    }

    fun onFlip() {
        cameraRepo.setTorch(false)
        lensFacing.update {
            if (it == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
            else CameraSelector.LENS_FACING_BACK
        }
    }

    fun onToggleFlash() {
        viewModelScope.launch { settingsRepo.setFlashEnabled(!uiState.value.flashEnabled) }
    }

    fun onToggleTorch() = cameraRepo.setTorch(!uiState.value.torchEnabled)
    fun onFocus(point: MeteringPoint) = cameraRepo.focusOn(point)
    fun onZoomBy(factor: Float) = cameraRepo.zoomBy(factor)
    fun onZoomTo(ratio: Float) = cameraRepo.setZoom(ratio)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as App).container
                CameraViewModel(
                    c.cameraRepository,
                    c.settingsRepository,
                    c.mediaRepository
                )
            }
        }
    }
}
