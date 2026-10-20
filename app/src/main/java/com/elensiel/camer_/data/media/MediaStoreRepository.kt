package com.elensiel.camer_.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
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
) : MediaRepository {

    override suspend fun save(
        photoFile: File,
        format: ImageFormat,
        saveDirectory: String,
    ): String = withContext(Dispatchers.IO) {
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

        // guarantees a failed save never leaves an empty gallery entry, whatever the cause.
        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                if (format.needsTranscoding) transcode(photoFile, format, outputStream)
                else photoFile.inputStream().use { it.copyTo(outputStream) }
            } ?: throw IOException("Failed to open output stream for $uri")
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }

        photoFile.delete() // delete cache
        uri.toString()
    }

    override suspend fun discard(photoFile: File): Unit =
        withContext(Dispatchers.IO) { photoFile.delete() }

    private fun transcode(src: File, format: ImageFormat, output: OutputStream) {
        // Resolved before decoding so an unsupported format fails early.
        val compressFormat = when (format) {
            ImageFormat.PNG -> Bitmap.CompressFormat.PNG
            ImageFormat.WEBP ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY
                else Bitmap.CompressFormat.WEBP

            else -> error("$format cannot be transcoded.")
        }

        val bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(src)) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            decodeLegacy(src)
        }

        try {
            check(bitmap.compress(compressFormat, 100, output)) { "Bitmap compression failed" }
        } finally {
            bitmap.recycle()
        }
    }

    // API 26-27: BitmapFactory + manual EXIF orientation
    private fun decodeLegacy(src: File): Bitmap {
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

        if (matrix.isIdentity) return decoded

        val rotated =
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        decoded.recycle()
        return rotated
    }
}
