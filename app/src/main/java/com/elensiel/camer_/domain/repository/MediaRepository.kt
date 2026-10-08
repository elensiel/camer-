package com.elensiel.camer_.domain.repository

import android.net.Uri
import com.elensiel.camer_.domain.model.ImageFormat
import java.io.File

interface MediaRepository {
    suspend fun save(photoFile: File, format: ImageFormat): Uri
    suspend fun discard(photoFile: File)
}
