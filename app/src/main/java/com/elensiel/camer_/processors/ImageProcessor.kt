package com.elensiel.camer_.processors

import java.io.File

fun interface ImageProcessor {
    suspend fun process(input: File): File
}
