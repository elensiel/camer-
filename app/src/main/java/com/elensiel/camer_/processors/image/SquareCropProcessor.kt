package com.elensiel.camer_.processors.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SquareCropProcessor : ImageProcessor {
    override suspend fun process(input: File): File = withContext(Dispatchers.IO) {
        val (width, height) = decodeBounds(input)
        val side = minOf(width, height)

        val left = (width - side) / 2
        val top = (height - side) / 2
        val cropRect = Rect(left, top, left + side, top + side)

        val regionDecoder = newRegionDecoder(input)
        val cropped =
            try {
                regionDecoder.decodeRegion(cropRect, null)
            } finally {
                regionDecoder.recycle()
            }

        FileOutputStream(input).use {
            cropped.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        cropped.recycle()

        input
    }

    private fun decodeBounds(file: File): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        return options.outWidth to options.outHeight
    }

    @Suppress("DEPRECATION")
    private fun newRegionDecoder(file: File): BitmapRegionDecoder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BitmapRegionDecoder.newInstance(file.absolutePath)
        } else {
            BitmapRegionDecoder.newInstance(file.absolutePath, false)
        }
}
