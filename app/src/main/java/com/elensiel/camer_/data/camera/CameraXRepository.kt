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
import com.elensiel.camer_.domain.model.CapturedPhoto
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

    /**
     * Binds the camera to [lifecycleOwner] and starts the preview.
     *
     * Steps:
     *  1. Get the provider and the camera selector
     *  2. Resolve the capture format (what the camera supports)
     *  3. Build the use cases (Preview + ImageCapture)
     *  4. Group them with a ViewPort so both crop the same region
     *  5. Unbind the old camera, bind the new one
     *  6. Publish state to the UI
     *  7. Collect zoom state (suspends until cancelled)
     *
     * This function SUSPENDS FOREVER: step 7 collects a flow that never completes.
     * It returns only when the calling coroutine is cancelled (LaunchedEffect keys
     * change: lens flip, aspect ratio, or format). Cancellation runs the `finally`
     * block, which clears the references. Call it from a long-lived coroutine.
     *
     * Every call does a full unbind/rebind.
     *
     * @param format requested output format. Falls back to JPEG if the camera
     *   can't produce it.
     */
    override suspend fun bind(
        lifecycleOwner: LifecycleOwner,
        lensFacing: LensFacing,
        aspectRatio: CaptureAspectRatio,
        format: ImageFormat,
    ) {
        // ---------------------------------------------------------------
        // 1. Provider and camera selector
        // ---------------------------------------------------------------

        // Suspends until CameraX's singleton provider is ready.
        val provider = ProcessCameraProvider.awaitInstance(context)

        // Built once: used to query capabilities (step 2) and to bind (step 5).
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing.toCameraX())
            .build()

        // ---------------------------------------------------------------
        // 2. Format resolution (per lens, so recomputed per bind)
        // ---------------------------------------------------------------

        val nativeFormats: Set<ImageFormat> = ImageCapture
            .getImageCaptureCapabilities(provider.getCameraInfo(cameraSelector))
            .supportedOutputFormats
            .mapNotNull { it.toImageFormat() }
            .toSet()

        // PNG/WEBP aren't native; presumably converted after capture, so always offered.
        val supportedFormats = nativeFormats + setOf(ImageFormat.PNG, ImageFormat.WEBP)

        // Fall back to JPEG if the camera can't produce the requested format.
        val effectiveFormat = if (format in supportedFormats) format else ImageFormat.JPEG

        // ---------------------------------------------------------------
        // 3. Use cases
        // ---------------------------------------------------------------

        // Shared by Preview and ImageCapture so both use the same ratio.
        // AUTO fallback picks the closest ratio if the exact one is unsupported.
        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    aspectRatio.toCameraXRatio(),
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()

        // Instead of a PreviewView, Preview hands us a SurfaceRequest, published
        // through _surfaceRequest. The Compose CameraXViewfinder collects it and
        // supplies the actual surface.
        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
            .also { it.setSurfaceProvider { request -> _surfaceRequest.value = request } }

        // MINIMIZE_LATENCY favors shutter speed over quality.
        // Flash mode is applied now so the current setting survives a rebind.
        // NOTE: setOutputFormat gets effectiveFormat, which may differ from
        // [format]. PNG/WEBP are not native, so verify they map to a native
        // format in toCameraXFormat().
        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(resolutionSelector)
            .setOutputFormat(effectiveFormat.toCameraXFormat())
            .build()
            .also { it.flashMode = flashMode() }

        // ---------------------------------------------------------------
        // 4. ViewPort + use case group
        // ---------------------------------------------------------------

        // Makes Preview and ImageCapture crop to the same region, so the saved
        // photo matches what the user saw. This is how 1:1 works: the sensor
        // streams 4:3 and the ViewPort crops to a square.
        // FILL_CENTER crops the excess evenly instead of letterboxing.
        val viewPort = ViewPort.Builder(aspectRatio.toViewPortRational(), preview.targetRotation)
            .setScaleType(ViewPort.FILL_CENTER)
            .build()

        // The ViewPort only takes effect when use cases are bound as a group.
        val useCaseGroup = UseCaseGroup.Builder()
            .setViewPort(viewPort)
            .addUseCase(preview)
            .addUseCase(capture)
            .build()

        // ---------------------------------------------------------------
        // 5. Rebind
        // ---------------------------------------------------------------

        // Release the previous binding (flip, ratio, or format change) first.
        provider.unbindAll()
        val cam = provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            useCaseGroup,
        )

        // Keep handles for the other methods (takePhoto, setTorch, focusOn, zoom).
        camera = cam
        imageCapture = capture
        imageFormat = effectiveFormat

        // ---------------------------------------------------------------
        // 6. Publish state
        // ---------------------------------------------------------------

        // Lets the UI hide the flash buttons or unsupported formats.
        _state.update {
            it.copy(
                hasFlashUnit = cam.cameraInfo.hasFlashUnit(),
                supportedFormats = supportedFormats
            )
        }

        // Re-apply the torch state, since unbindAll() turned it off.
        cam.cameraControl.enableTorch(_state.value.torchEnabled)

        // ---------------------------------------------------------------
        // 7. Zoom collection (never completes, so bind() never returns)
        // ---------------------------------------------------------------

        try {
            // Mirror CameraX's zoom state (pinch or preset) into our state.
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
            // Runs on cancellation. The identity check (===) stops an old,
            // cancelled bind() from wiping the handles of a NEWER bind() that
            // already replaced them.
            if (camera === cam) {
                camera = null
                imageCapture = null
                imageFormat = null
            }
        }
    }

    override suspend fun takePhoto(): CapturedPhoto = suspendCancellableCoroutine { cont ->
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

        // captured photo is still a native format at this point
        val tempFileExtension =
            if (format.needsTranscoding) "jpg"
            else format.fileExtension

        // create and store the photo in cache
        val photoFile = File(
            context.cacheDir,
            "$name.${tempFileExtension}"
        )

        capture.takePicture(
            ImageCapture.OutputFileOptions.Builder(photoFile).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    cont.resume(CapturedPhoto(photoFile, format))
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
