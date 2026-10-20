package com.elensiel.camer_

import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.LifecycleOwner
import com.elensiel.camer_.domain.model.CameraState
import com.elensiel.camer_.domain.model.CaptureAspectRatio
import com.elensiel.camer_.domain.model.CaptureResult
import com.elensiel.camer_.domain.model.CapturedPhoto
import com.elensiel.camer_.domain.model.FlashMode
import com.elensiel.camer_.domain.model.ImageFormat
import com.elensiel.camer_.domain.model.LensFacing
import com.elensiel.camer_.domain.model.Settings
import com.elensiel.camer_.domain.model.VolumeKeyShutter
import com.elensiel.camer_.domain.repository.CameraRepository
import com.elensiel.camer_.domain.repository.MediaRepository
import com.elensiel.camer_.domain.repository.SettingsRepository
import com.elensiel.camer_.domain.usecase.CapturePhotoUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException

class CapturePhotoUseCaseTest {

    private val file = File("photo.jpg")
    private val photo = CapturedPhoto(file, ImageFormat.JPEG)

    @Test
    fun alwaysSave_saveFails_discardsFile() = runTest {
        val media = FakeMedia(saveError = IOException("disk full"))
        val result = useCase(alwaysSave = true, media = media)()

        assertTrue(result is CaptureResult.Failed)
        assertEquals(listOf(file), media.discarded)
    }

    @Test
    fun alwaysSave_saveSucceeds_doesNotDiscard() = runTest {
        val media = FakeMedia()
        val result = useCase(alwaysSave = true, media = media)()

        assertTrue(result is CaptureResult.Saved)
        assertTrue(media.discarded.isEmpty()) // save() deletes the cache file itself
    }

    @Test
    fun review_returnsPending_doesNotDiscard() = runTest {
        val media = FakeMedia()
        val result = useCase(alwaysSave = false, media = media)()

        assertTrue(result is CaptureResult.Pending)
        assertTrue(media.discarded.isEmpty())
    }

    @Test
    fun captureFails_returnsFailed_neverTouchesMedia() = runTest {
        val media = FakeMedia()
        val result = useCase(alwaysSave = true, media = media, captureError = IOException())()

        assertTrue(result is CaptureResult.Failed)
        assertTrue(media.saved.isEmpty())
        assertTrue(media.discarded.isEmpty())
    }

    // ---- helpers ----

    private fun useCase(
        alwaysSave: Boolean,
        media: FakeMedia,
        captureError: Throwable? = null,
    ) = CapturePhotoUseCase(
        FakeCamera(photo, captureError),
        FakeSettings(Settings(alwaysSaveEnabled = alwaysSave)),
        media,
    )

    private class FakeMedia(private val saveError: Throwable? = null) : MediaRepository {
        val saved = mutableListOf<File>()
        val discarded = mutableListOf<File>()

        override suspend fun save(
            photoFile: File,
            format: ImageFormat,
            saveDirectory: String,
        ): String {
            saved += photoFile
            saveError?.let { throw it }
            return "content://fake"
        }

        override suspend fun discard(photoFile: File) {
            discarded += photoFile
        }
    }

    private class FakeCamera(
        private val photo: CapturedPhoto,
        private val error: Throwable?,
    ) : CameraRepository {
        override val surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
        override val state = MutableStateFlow(CameraState())
        override val captureStarted = MutableSharedFlow<Unit>()

        override suspend fun takePhoto(): CapturedPhoto = error?.let { throw it } ?: photo

        override suspend fun bind(
            o: LifecycleOwner,
            l: LensFacing,
            a: CaptureAspectRatio,
            f: ImageFormat,
        ) = Unit

        override suspend fun getSupportedFormats(lensFacing: LensFacing) = emptySet<ImageFormat>()
        override fun focusOn(x: Float, y: Float) = Unit
        override fun setFlashMode(mode: FlashMode) = Unit
        override fun setTorch(enabled: Boolean) = Unit
        override fun setShutterSound(enabled: Boolean) = Unit
        override fun setZoom(ratio: Float) = Unit
        override fun zoomBy(factor: Float) = Unit
    }

    private class FakeSettings(s: Settings) : SettingsRepository {
        override val settings = flowOf(s)
        override suspend fun setAlwaysSaveEnabled(enabled: Boolean) = Unit
        override suspend fun setAspectRatio(ratio: CaptureAspectRatio) = Unit
        override suspend fun setFlashMode(mode: FlashMode) = Unit
        override suspend fun setImageFormat(format: ImageFormat) = Unit
        override suspend fun setSaveLocation(location: String) = Unit
        override suspend fun setShutterSoundEnabled(enabled: Boolean) = Unit
        override suspend fun setVolumeKeyShutter(mode: VolumeKeyShutter) = Unit
    }
}
