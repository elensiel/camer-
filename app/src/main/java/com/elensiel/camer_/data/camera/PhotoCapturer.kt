package com.elensiel.camer_.data.camera

import android.content.Context
import android.media.MediaActionSound
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import com.elensiel.camer_.domain.model.CapturedPhoto
import com.elensiel.camer_.domain.model.ImageFormat
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class PhotoCapturer(
    private val context: Context,
    private val session: CameraSession,
    private val onCaptureStarted: () -> Unit,
) {

    private val shutterSound = MediaActionSound().apply { load(MediaActionSound.SHUTTER_CLICK) }
    var shutterSoundEnabled = false

    suspend fun takePhoto(): CapturedPhoto = suspendCancellableCoroutine { cont ->
        val capture = session.imageCapture
        val format = session.imageFormat

        if (capture == null || format == null) {
            cont.resumeWithException(IllegalStateException("Camera not bound."))
            return@suspendCancellableCoroutine
        }

        val photoFile = newCacheFile(format)

        capture.takePicture(
            ImageCapture.OutputFileOptions.Builder(photoFile).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onCaptureStarted() {
                    this@PhotoCapturer.onCaptureStarted()
                    if (shutterSoundEnabled) shutterSound.play(MediaActionSound.SHUTTER_CLICK)
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Cancelled while the file was still being written: nobody will own it.
                    if (cont.isActive) cont.resume(CapturedPhoto(photoFile, format))
                    else photoFile.delete()
                }

                override fun onError(exception: ImageCaptureException) {
                    photoFile.delete()
                    cont.resumeWithException(exception)
                }
            }
        )
    }

    private fun newCacheFile(format: ImageFormat): File {
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
            .format(System.currentTimeMillis())

        // Transcoded formats are captured as JPEG first; MediaRepository converts on save.
        val extension = if (format.needsTranscoding) "jpg" else format.fileExtension
        return File(context.cacheDir, "$name.$extension")
    }
}
