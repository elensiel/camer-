package com.elensiel.camer_.processors.image

import java.io.File

fun interface ImageProcessor {
    suspend fun process(input: File): File
}
