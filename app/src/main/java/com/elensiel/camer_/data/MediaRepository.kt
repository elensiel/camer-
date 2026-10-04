package com.elensiel.camer_.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class MediaRepository(
    private val context: Context,
    private val mimeType: String,
    private val saveDirectory: String,
) {
    suspend fun save(photoFile: File): Uri = withContext(Dispatchers.IO) {
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

    suspend fun discard(photoFile: File) = withContext(Dispatchers.IO) { photoFile.delete() }
}
