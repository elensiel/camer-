package com.elensiel.camer_.domain.usecase

import com.elensiel.camer_.domain.model.CaptureResult
import com.elensiel.camer_.domain.repository.CameraRepository
import com.elensiel.camer_.domain.repository.MediaRepository
import com.elensiel.camer_.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class CapturePhotoUseCase(
    private val cameraRepo: CameraRepository,
    private val settingsRepo: SettingsRepository,
    private val mediaRepo: MediaRepository,
) {
    /**
     * Takes a photo, then either saves it right away (Always Save on) or
     * returns it as [CaptureResult.Pending] for the user to review.
     */
    suspend operator fun invoke(): CaptureResult {
        val settings = settingsRepo.settings.first()

        val photo = runCatching { cameraRepo.takePhoto() }
            .getOrElse { return CaptureResult.Failed(it) }

        if (!settings.alwaysSaveEnabled) return CaptureResult.Pending(photo)

        return runCatching { mediaRepo.save(photo.file, photo.format, settings.saveLocation) }
            .fold(
                onSuccess = { CaptureResult.Saved(it) },
                onFailure = {
                    mediaRepo.discard(photo.file)
                    CaptureResult.Failed(it)
                },
            )
    }
}
