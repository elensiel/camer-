package com.elensiel.camer_.data

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class CameraState(
    val hasFlashUnit: Boolean = true,
    val torchEnabled: Boolean = false,
    val zoomRatio: Float = 1f,
    val minZoomRatio: Float = 1f,
    val maxZoomRatio: Float = 1f,
)

class CameraRepository(private val context: Context) {
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var flashEnabled = false

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

    private val _state = MutableStateFlow(CameraState())
    val state = _state.asStateFlow()

    suspend fun bind(
        lifecycleOwner: LifecycleOwner,
        lensFacing: Int,
        aspectRatio: AppAspectRatio,
    ) {
        val provider = ProcessCameraProvider.awaitInstance(context)

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    aspectRatio.ratioInt,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
            .also { it.setSurfaceProvider { request -> _surfaceRequest.value = request } }

        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(resolutionSelector)
            .build()
            .also { it.flashMode = flashMode() }

        val viewPort = ViewPort.Builder(aspectRatio.viewPortRational, preview.targetRotation)
            .setScaleType(ViewPort.FILL_CENTER)
            .build()

        val useCaseGroup = UseCaseGroup.Builder()
            .setViewPort(viewPort)
            .addUseCase(preview)
            .addUseCase(capture)
            .build()

        provider.unbindAll()
        val cam = provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.Builder().requireLensFacing(lensFacing).build(),
            useCaseGroup,
        )
        camera = cam
        imageCapture = capture

        _state.update { it.copy(hasFlashUnit = cam.cameraInfo.hasFlashUnit()) }
        cam.cameraControl.enableTorch(_state.value.torchEnabled)

        try {
            cam.cameraInfo.zoomState.asFlow().collect { z ->
                _state.update {
                    it.copy(
                        zoomRatio = z.zoomRatio,
                        minZoomRatio = z.minZoomRatio,
                        maxZoomRatio = z.maxZoomRatio,
                    )
                }
            }
        } finally {
            if (camera === cam) {
                camera = null
                imageCapture = null
            }
        }
    }

    suspend fun takePhoto(): File = suspendCancellableCoroutine { cont ->
        val capture = imageCapture ?: run {
            cont.resumeWithException(IllegalStateException("Camera not bound."))
            return@suspendCancellableCoroutine
        }

        val name = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS",
            Locale.getDefault()
        ).format(System.currentTimeMillis())

        val photoFile = File(
            context.cacheDir,
            "$name.jpg"
        )

        capture.takePicture(
            ImageCapture.OutputFileOptions.Builder(photoFile).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    cont.resume(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    cont.resumeWithException(exception)
                }
            }
        )
    }

    fun setFlashEnabled(enabled: Boolean) {
        flashEnabled = enabled
        imageCapture?.flashMode = flashMode()
    }

    fun setTorch(enabled: Boolean) {
        if (!_state.value.hasFlashUnit) return
        _state.update { it.copy(torchEnabled = enabled) }
        camera?.cameraControl?.enableTorch(enabled)
    }

    fun focusOn(point: MeteringPoint) {
        camera?.cameraControl?.startFocusAndMetering(
            FocusMeteringAction.Builder(
                point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
            ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()
        )
    }

    fun setZoom(ratio: Float) {
        val s = _state.value
        camera?.cameraControl?.setZoomRatio(ratio.coerceIn(s.minZoomRatio, s.maxZoomRatio))
    }

    fun zoomBy(factor: Float) = setZoom(_state.value.zoomRatio * factor)

    private fun flashMode() =
        if (flashEnabled) ImageCapture.FLASH_MODE_ON
        else ImageCapture.FLASH_MODE_OFF
}
