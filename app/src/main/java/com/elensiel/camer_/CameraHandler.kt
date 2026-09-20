package com.elensiel.camer_

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraHandler(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
) {
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val preview = Preview.Builder().build()
    private var imageCapture: ImageCapture? = null

    var cameraSelector by mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA)
        private set
    var hasFlashUnit by mutableStateOf(true)
        private set
    var flashEnabled by mutableStateOf(false)
        private set
    var torchEnabled by mutableStateOf(false)
        private set

    suspend fun startCamera() {
        cameraProvider = ProcessCameraProvider.awaitInstance(context)
        bindCamera()
    }

    fun setSurfaceProvider(surfaceProvider: Preview.SurfaceProvider) {
        preview.surfaceProvider = surfaceProvider
    }

    // takes a shot
    // but does not save the photo immediately
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
            })
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

    private fun bindCamera() {
        val provider = cameraProvider ?: return

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        provider.unbindAll()

        camera = provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
        )

        hasFlashUnit = camera!!.cameraInfo.hasFlashUnit()
    }
}
