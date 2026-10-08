package com.elensiel.camer_.data.camera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
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
import com.elensiel.camer_.domain.model.CameraState
import com.elensiel.camer_.domain.model.CaptureAspectRatio
import com.elensiel.camer_.domain.model.CapturedImage
import com.elensiel.camer_.domain.model.ImageFormat
import com.elensiel.camer_.domain.model.LensFacing
import com.elensiel.camer_.domain.repository.CameraRepository
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

class CameraXRepository(private val context: Context) : CameraRepository {
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var flashEnabled = false
    private var imageFormat: ImageFormat? = null

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    override val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

    private val _state = MutableStateFlow(CameraState())
    override val state = _state.asStateFlow()

    override suspend fun bind(
        lifecycleOwner: LifecycleOwner,
        lensFacing: LensFacing,
        aspectRatio: CaptureAspectRatio,
        format: ImageFormat,
    ) {
        val provider = ProcessCameraProvider.awaitInstance(context)

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    aspectRatio.toCameraXRatio(),
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
            .also { it.setSurfaceProvider { request -> _surfaceRequest.value = request } }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing.toCameraX())
            .build()

        val supportedFormats: Set<ImageFormat> = ImageCapture
            .getImageCaptureCapabilities(provider.getCameraInfo(cameraSelector))
            .supportedOutputFormats
            .mapNotNull { it.toImageFormat() }
            .toSet()

        val effectiveFormat = if (format in supportedFormats) format else ImageFormat.JPEG

        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(resolutionSelector)
            .setOutputFormat(effectiveFormat.toCameraXFormat())
            .build()
            .also { it.flashMode = flashMode() }

        val viewPort = ViewPort.Builder(aspectRatio.toViewPortRational(), preview.targetRotation)
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
            cameraSelector,
            useCaseGroup,
        )
        camera = cam
        imageCapture = capture
        imageFormat = effectiveFormat

        _state.update {
            it.copy(
                hasFlashUnit = cam.cameraInfo.hasFlashUnit(),
                supportedFormats = supportedFormats
            )
        }
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
                imageFormat = null
            }
        }
    }

    override suspend fun takePhoto(): CapturedImage = suspendCancellableCoroutine { cont ->
        val capture = imageCapture
        val format = imageFormat

        if (capture == null || format == null) {
            cont.resumeWithException(IllegalStateException("Camera not bound."))
            return@suspendCancellableCoroutine
        }

        val name = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS",
            Locale.getDefault()
        ).format(System.currentTimeMillis())

        val photoFile = File(
            context.cacheDir,
            "$name.${format.fileExtension}"
        )

        capture.takePicture(
            ImageCapture.OutputFileOptions.Builder(photoFile).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    cont.resume(CapturedImage(photoFile, format))
                }

                override fun onError(exception: ImageCaptureException) {
                    cont.resumeWithException(exception)
                }
            }
        )
    }

    override fun setFlashEnabled(enabled: Boolean) {
        flashEnabled = enabled
        imageCapture?.flashMode = flashMode()
    }

    override fun setTorch(enabled: Boolean) {
        if (!_state.value.hasFlashUnit) return
        _state.update { it.copy(torchEnabled = enabled) }
        camera?.cameraControl?.enableTorch(enabled)
    }

    override fun focusOn(x: Float, y: Float) {
        val res = _surfaceRequest.value?.resolution ?: return

        val point = SurfaceOrientedMeteringPointFactory(
            res.width.toFloat(),
            res.height.toFloat()
        ).createPoint(x, y)

        camera?.cameraControl?.startFocusAndMetering(
            FocusMeteringAction.Builder(
                point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
            ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()
        )
    }

    override fun setZoom(ratio: Float) {
        val s = _state.value
        camera?.cameraControl?.setZoomRatio(ratio.coerceIn(s.minZoomRatio, s.maxZoomRatio))
    }

    override fun zoomBy(factor: Float) = setZoom(_state.value.zoomRatio * factor)

    private fun flashMode() =
        if (flashEnabled) ImageCapture.FLASH_MODE_ON
        else ImageCapture.FLASH_MODE_OFF
}
