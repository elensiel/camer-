package com.elensiel.camer_

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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

            val outputStream = resolver.openOutputStream(uri)
                ?: throw IOException("Failed to open output stream for $uri")

            outputStream.use { output ->
                photoFile.inputStream().use { input ->
                    input.copyTo(output)
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
