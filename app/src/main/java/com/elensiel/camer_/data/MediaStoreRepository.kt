package com.elensiel.camer_.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.elensiel.camer_.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class MediaStoreRepository(
    private val context: Context,
    private val mimeType: String,
    private val saveDirectory: String,
) : MediaRepository {
    override suspend fun save(photoFile: File): Uri = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "${photoFile.nameWithoutExtension}.${mimeType}",
            )
            put(
                MediaStore.Images.Media.MIME_TYPE,
                "image/${mimeType}",
            )
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                saveDirectory,
            )
        }

        val resolver = context.contentResolver

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Failed to create MediaStore entry.")

        resolver.openOutputStream(uri)?.use { outputStream ->
            photoFile.inputStream().use { it.copyTo(outputStream) }
        } ?: throw IOException("Failed to open output stream for $uri")

        photoFile.delete()
        uri
    }

    override suspend fun discard(photoFile: File): Unit =
        withContext(Dispatchers.IO) { photoFile.delete() }
}
