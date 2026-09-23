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

// Crops the input file to a centered square, overwriting it in place.
// Used when the user has selected the 1:1 aspect ratio, since CameraX
// has no native square capture mode — capture always happens at 4:3
// or 16:9 and gets cropped down afterward.
class CropToSquareProcessor : ImageProcessor {
    @RequiresApi(Build.VERSION_CODES.S)
    override suspend fun process(input: File): File = withContext(Dispatchers.IO) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(input.path, bounds)

        val height = bounds.outHeight
        val width = bounds.outWidth
        if (width <= 0 || height <= 0) return@withContext input

        val size = minOf(width, height)
        val left = (width - size) / 2
        val top = (height - size) / 2
        val cropRect = Rect(left, top, left + size, top + size)

        val regionDecoder = BitmapRegionDecoder.newInstance(input.path)
            ?: return@withContext input
        val cropped = regionDecoder.decodeRegion(cropRect, null)
        regionDecoder.recycle()

        if (cropped == null) return@withContext input

        FileOutputStream(input).use {output ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }

        cropped.recycle()

        input
    }
}
