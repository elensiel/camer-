package com.elensiel.camer_

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.elensiel.camer_.data.AppAspectRatio
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class CameraHandler(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
) {

    // ---------------------------------------------------------------
    // Core camera objects
    // ---------------------------------------------------------------

    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    var preview by mutableStateOf(Preview.Builder().build())
        private set
    var cameraSelector by mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA)
        private set

    suspend fun startCamera() {
        cameraProvider = ProcessCameraProvider.awaitInstance(context)
        bindCamera()
    }

    fun setSurfaceProvider(surfaceProvider: Preview.SurfaceProvider) {
        preview.surfaceProvider = surfaceProvider
    }

    private fun bindCamera() {
        val provider = cameraProvider ?: return

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    aspectRatio.ratioInt,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO,
                )
            )
            .build()

        preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(resolutionSelector)
            .build()

        provider.unbindAll()

        camera = provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
        )

        hasFlashUnit = camera!!.cameraInfo.hasFlashUnit()

        // sync zoom ratio with actual camera
        camera!!.cameraInfo.zoomState.observe(lifecycleOwner) { state ->
            zoomRatio = state.zoomRatio
            minZoomRatio = state.minZoomRatio
            maxZoomRatio = state.maxZoomRatio
        }
    }

    fun flipCamera() {
        cameraSelector =
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

        // disable torch upon flipping
        torchEnabled = false

        bindCamera()
    }

    // ---------------------------------------------------------------
    // Aspect ratio
    // ---------------------------------------------------------------

    var aspectRatio by mutableStateOf(AppAspectRatio.RATIO_4_3)
        private set

    fun applyAspectRatio(ratio: AppAspectRatio) {
        if (aspectRatio == ratio) return
        aspectRatio = ratio
        bindCamera()
    }

//    fun cycleAspectRatio() {
//        val values = AppAspectRatio.entries
//        val next = values[(values.indexOf(aspectRatio) + 1) % values.size]
//        applyAspectRatio(next)
//    }

    // ---------------------------------------------------------------
    // Capture
    // ---------------------------------------------------------------

    // Takes a shot but does not save the photo immediately;
    // caller decides what to do with the returned file.
    fun takePhoto(
        onPhotoCaptured: (File) -> Unit,
        onError: (ImageCaptureException) -> Unit,
    ) {
        if (imageCapture == null) return

        val name = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS",
            Locale.getDefault()
        ).format(System.currentTimeMillis())

        val photoFile = File(
            context.cacheDir,
            "$name.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(photoFile)
            .build()

        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onPhotoCaptured(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    // ---------------------------------------------------------------
    // Flash / torch
    // ---------------------------------------------------------------

    var hasFlashUnit by mutableStateOf(true)
        private set
    var flashEnabled by mutableStateOf(false)
        private set
    var torchEnabled by mutableStateOf(false)
        private set

    fun toggleFlashMode() {
        if (!hasFlashUnit) return

        flashEnabled = !flashEnabled

        imageCapture?.flashMode =
            if (flashEnabled) {
                ImageCapture.FLASH_MODE_ON
            } else {
                ImageCapture.FLASH_MODE_OFF
            }
    }

    fun toggleTorch() {
        if (!hasFlashUnit) return

        torchEnabled = !torchEnabled
        camera?.cameraControl?.enableTorch(torchEnabled)
    }

    // ---------------------------------------------------------------
    // Zoom
    // ---------------------------------------------------------------

    var zoomRatio by mutableFloatStateOf(1f)
        private set
    var minZoomRatio by mutableFloatStateOf(1f)
        private set
    var maxZoomRatio by mutableFloatStateOf(1f)
        private set

    fun zoomBy(scaleFactor: Float) = applyZoomRatio(zoomRatio * scaleFactor)

    fun resetZoom() = applyZoomRatio(1f)

    private fun applyZoomRatio(ratio: Float) {
        val cam = camera ?: return
        val state = cam.cameraInfo.zoomState.value ?: return
        val clamped = ratio.coerceIn(state.minZoomRatio, state.maxZoomRatio)
        cam.cameraControl.setZoomRatio(clamped)
    }

    // used by zoom slider ui
    fun setLinearZoom(linear: Float) {
        camera?.cameraControl?.setLinearZoom(linear.coerceIn(0f, 1f))
    }

    // ---------------------------------------------------------------
    // Focus
    // ---------------------------------------------------------------

    fun focusOnPoint(point: MeteringPoint) {
        val cam = camera ?: return

        val action = FocusMeteringAction.Builder(
            point,
            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
        )
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()

        cam.cameraControl.startFocusAndMetering(action)
    }
}
