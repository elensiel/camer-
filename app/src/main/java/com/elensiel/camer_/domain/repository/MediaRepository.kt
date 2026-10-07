package com.elensiel.camer_.domain.repository

import android.net.Uri
import java.io.File

interface MediaRepository {
    suspend fun save(photoFile: File): Uri
    suspend fun discard(photoFile: File)
}
