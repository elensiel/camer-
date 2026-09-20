package com.elensiel.camer_

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.IOException

class CapturedImageHandler(
    private val context: Context,
    private val mimeType: String,
    private val saveDirectory: String,
) {
    // saves the photo
    // that came from CameraHandler.takePhoto()
    fun saveImage(
        photoFile: File,
        onSaved: (Uri) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        try {
            val name = photoFile.nameWithoutExtension
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name.$mimeType")
                put(MediaStore.Images.Media.MIME_TYPE, "image/$mimeType")
                put(MediaStore.Images.Media.RELATIVE_PATH, saveDirectory)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues,
            ) ?: throw IOException("Failed to create MediaStore entry.")

            resolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException("Failed to open output stream.")

                photoFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            photoFile.delete()
            onSaved(uri)

        } catch (e: Exception) {
            onError(e)
        }
    }

    fun discardPhoto(photoFile: File) {
        photoFile.delete()
    }
}
