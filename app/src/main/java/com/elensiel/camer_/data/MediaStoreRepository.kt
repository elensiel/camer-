package com.elensiel.camer_.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.elensiel.camer_.domain.model.ImageFormat
import com.elensiel.camer_.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.OutputStream

class MediaStoreRepository(
    private val context: Context,
    private val saveDirectory: String,
) : MediaRepository {
    override suspend fun save(
        photoFile: File,
        format: ImageFormat,
    ): Uri = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "${photoFile.nameWithoutExtension}.${format.fileExtension}",
            )
            put(
                MediaStore.Images.Media.MIME_TYPE,
                format.mimeType,
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
            if (format.needsTranscoding) transcode(photoFile, format, outputStream)
            else photoFile.inputStream().use { it.copyTo(outputStream) }
        } ?: throw IOException("Failed to open output stream for $uri")

        photoFile.delete() // delete cache
        uri
    }

    override suspend fun discard(photoFile: File): Unit =
        withContext(Dispatchers.IO) { photoFile.delete() }

    private fun transcode(src: File, format: ImageFormat, output: OutputStream) {
        val decoded = BitmapFactory.decodeFile(src.absolutePath)
            ?: throw IOException("Failed to decode ${src.name}")

        val matrix = Matrix()
        when (ExifInterface(src.absolutePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(1f, -1f)
            }
        }
        val bitmap = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)

        val compressFormat = when (format) {
            ImageFormat.PNG -> Bitmap.CompressFormat.PNG
            else ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    Bitmap.CompressFormat.WEBP
                }
        }

        check(bitmap.compress(compressFormat, 100, output)) { "Bitmap compression failed" }
        if (bitmap !== decoded) decoded.recycle()
        bitmap.recycle()
    }
}
