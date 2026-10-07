package com.elensiel.camer_.domain.repository

import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.LifecycleOwner
import com.elensiel.camer_.domain.model.CameraState
import com.elensiel.camer_.domain.model.CaptureAspectRatio
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface CameraRepository {
    val surfaceRequest: StateFlow<SurfaceRequest?>
    val state: StateFlow<CameraState>

    suspend fun bind(
        lifecycleOwner: LifecycleOwner,
        lensFacing: Int,
        aspectRatio: CaptureAspectRatio,
    )

    suspend fun takePhoto(): File
    fun setFlashEnabled(enabled: Boolean)
    fun setTorch(enabled: Boolean)
    fun focusOn(x: Float, y: Float)
    fun setZoom(ratio: Float)
    fun zoomBy(factor: Float)
}
